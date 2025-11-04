/**
 * Script để generate Postman Collection từ Swagger/OpenAPI
 * 
 * Usage:
 *   node scripts/generate-postman.js
 *   node scripts/generate-postman.js --url http://localhost:8080
 *   node scripts/generate-postman.js --url http://localhost:8080 --output phonehub.postman.json
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');

// Parse command line arguments
const args = process.argv.slice(2);
const urlIndex = args.indexOf('--url');
const outputIndex = args.indexOf('--output');

const DEFAULT_URL = 'http://localhost:8080';
const DEFAULT_OUTPUT = 'phonehub.postman.json';
const API_DOCS_PATH = '/api-docs';

const baseUrl = urlIndex !== -1 ? args[urlIndex + 1] : DEFAULT_URL;
const outputFile = outputIndex !== -1 ? args[outputIndex + 1] : DEFAULT_OUTPUT;

console.log('🚀 Generating Postman Collection from Swagger/OpenAPI...');
console.log(`📡 Fetching from: ${baseUrl}${API_DOCS_PATH}`);
console.log(`📄 Output file: ${outputFile}`);

/**
 * Fetch OpenAPI spec from server
 */
function fetchOpenAPISpec(url) {
    return new Promise((resolve, reject) => {
        const client = url.startsWith('https') ? https : http;
        
        client.get(url, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                if (res.statusCode === 200) {
                    try {
                        const spec = JSON.parse(data);
                        resolve(spec);
                    } catch (e) {
                        reject(new Error(`Failed to parse JSON: ${e.message}`));
                    }
                } else {
                    reject(new Error(`HTTP ${res.statusCode}: ${res.statusMessage}`));
                }
            });
        }).on('error', (err) => {
            reject(new Error(`Request failed: ${err.message}`));
        });
    });
}

/**
 * Convert OpenAPI spec to Postman Collection v2.1
 */
function convertToPostman(openAPISpec, baseUrl) {
    const collection = {
        info: {
            name: openAPISpec.info?.title || 'API Collection',
            description: openAPISpec.info?.description || '',
            schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
            _exporter_id: 'phonehub-swagger-generator'
        },
        item: [],
        variable: []
    };

    // Add base URL variable
    const serverUrl = openAPISpec.servers?.[0]?.url || baseUrl;
    collection.variable.push({
        key: 'baseUrl',
        value: serverUrl,
        type: 'string'
    });

    // Group endpoints by tags
    const tagGroups = {};
    
    if (openAPISpec.paths) {
        Object.entries(openAPISpec.paths).forEach(([path, methods]) => {
            Object.entries(methods).forEach(([method, operation]) => {
                if (['get', 'post', 'put', 'patch', 'delete', 'head', 'options'].includes(method.toLowerCase())) {
                    const tags = operation.tags || ['Default'];
                    const tag = tags[0];
                    
                    if (!tagGroups[tag]) {
                        tagGroups[tag] = {
                            name: tag,
                            item: []
                        };
                    }
                    
                    const request = {
                        name: operation.summary || operation.operationId || `${method.toUpperCase()} ${path}`,
                        request: {
                            method: method.toUpperCase(),
                            header: [],
                            url: {
                                raw: '{{baseUrl}}' + path,
                                host: ['{{baseUrl}}'],
                                path: path.split('/').filter(p => p)
                            },
                            description: operation.description || ''
                        },
                        response: []
                    };

                    // Add parameters
                    if (operation.parameters) {
                        operation.parameters.forEach(param => {
                            if (param.in === 'header') {
                                request.request.header.push({
                                    key: param.name,
                                    value: param.example || '',
                                    description: param.description || '',
                                    type: 'text'
                                });
                            } else if (param.in === 'query') {
                                if (!request.request.url.query) {
                                    request.request.url.query = [];
                                }
                                request.request.url.query.push({
                                    key: param.name,
                                    value: param.example || '',
                                    description: param.description || '',
                                    disabled: !param.required
                                });
                            } else if (param.in === 'path') {
                                // Path parameters are already in the path
                                request.request.url.path = request.request.url.path.map(p => {
                                    if (p === `{${param.name}}` || p === `:${param.name}`) {
                                        return `:${param.name}`;
                                    }
                                    return p;
                                });
                            }
                        });
                    }

                    // Add request body
                    if (operation.requestBody) {
                        const content = operation.requestBody.content;
                        if (content) {
                            const contentType = Object.keys(content)[0] || 'application/json';
                            const schema = content[contentType].schema;
                            
                            request.request.header.push({
                                key: 'Content-Type',
                                value: contentType,
                                type: 'text'
                            });

                            if (schema) {
                                request.request.body = {
                                    mode: 'raw',
                                    raw: generateExampleFromSchema(schema),
                                    options: {
                                        raw: {
                                            language: contentType.includes('json') ? 'json' : 'text'
                                        }
                                    }
                                };
                            }
                        }
                    }

                    // Add security (JWT Bearer Token)
                    if (operation.security || openAPISpec.security) {
                        request.request.header.push({
                            key: 'Authorization',
                            value: 'Bearer {{accessToken}}',
                            type: 'text',
                            description: 'JWT Bearer Token'
                        });
                    }

                    // Add example responses
                    if (operation.responses) {
                        Object.entries(operation.responses).forEach(([statusCode, response]) => {
                            request.response.push({
                                name: `${statusCode} - ${response.description || 'Response'}`,
                                originalRequest: request.request,
                                status: statusCode,
                                code: parseInt(statusCode),
                                _postman_previewlanguage: 'json',
                                header: [],
                                body: response.content?.['application/json']?.schema 
                                    ? JSON.stringify(generateExampleFromSchema(response.content['application/json'].schema), null, 2)
                                    : ''
                            });
                        });
                    }

                    tagGroups[tag].item.push(request);
                }
            });
        });
    }

    // Convert tag groups to Postman folders
    collection.item = Object.values(tagGroups);

    return collection;
}

/**
 * Generate example JSON from JSON Schema
 */
function generateExampleFromSchema(schema) {
    if (!schema) return '{}';
    
    if (schema.type === 'object' && schema.properties) {
        const example = {};
        Object.entries(schema.properties).forEach(([key, prop]) => {
            if (prop.example !== undefined) {
                example[key] = prop.example;
            } else if (prop.type === 'string') {
                example[key] = prop.format === 'email' ? 'example@email.com' : 
                              prop.format === 'date' ? '2024-01-01' :
                              prop.format === 'date-time' ? '2024-01-01T00:00:00Z' :
                              'string';
            } else if (prop.type === 'number' || prop.type === 'integer') {
                example[key] = 0;
            } else if (prop.type === 'boolean') {
                example[key] = false;
            } else if (prop.type === 'array') {
                example[key] = [];
            } else if (prop.type === 'object') {
                example[key] = generateExampleFromSchema(prop);
            }
        });
        return JSON.stringify(example, null, 2);
    }
    
    return '{}';
}

/**
 * Main function
 */
async function main() {
    try {
        // Fetch OpenAPI spec
        const openAPISpec = await fetchOpenAPISpec(`${baseUrl}${API_DOCS_PATH}`);
        console.log('✅ Fetched OpenAPI spec successfully');
        
        // Convert to Postman collection
        const collection = convertToPostman(openAPISpec, baseUrl);
        console.log(`✅ Generated Postman collection with ${collection.item.length} folders`);
        
        // Count total requests
        const totalRequests = collection.item.reduce((sum, folder) => sum + folder.item.length, 0);
        console.log(`📊 Total requests: ${totalRequests}`);
        
        // Write to file
        const outputPath = path.resolve(outputFile);
        fs.writeFileSync(outputPath, JSON.stringify(collection, null, 2), 'utf8');
        console.log(`✅ Postman collection saved to: ${outputPath}`);
        
        console.log('\n🎉 Done! Import the collection file into Postman to start testing.');
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        console.error('\n💡 Make sure:');
        console.error('   1. The server is running at', baseUrl);
        console.error('   2. Swagger/OpenAPI is enabled');
        console.error('   3. The /api-docs endpoint is accessible');
        process.exit(1);
    }
}

// Run script
main();

