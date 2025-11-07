package com.example.phonehub.utils;

import com.example.phonehub.dto.*;
import com.example.phonehub.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class ProductUtils {
    public static ProductDto toDto(Product p){
        if (p==null) return null; ProductDto d=new ProductDto();
        d.setId(p.getId()); d.setName(p.getName()); d.setSlug(p.getSlug()); d.setBrand(p.getBrand());
        if (p.getCategory()!=null) d.setCategory(CategoryUtils.toDto(p.getCategory()));
        d.setPrice(p.getPrice()); d.setPriceOld(p.getPriceOld()); d.setDiscount(p.getDiscount());
        d.setThumbnailImage(p.getThumbnailImage()); d.setQuantity(p.getQuantity()); d.setIsPublished(p.getIsPublished()); d.setPublishedAt(p.getPublishedAt());
        if (p.getCreatedBy()!=null) d.setCreatedBy(UserUtils.toDto(p.getCreatedBy()));
        d.setCreatedAt(p.getCreatedAt()); d.setUpdatedAt(p.getUpdatedAt());
        if (p.getSpecifications()!=null) d.setSpecifications(toSpecList(p.getSpecifications()));
        if (p.getColors()!=null) d.setColors(toColorList(p.getColors()));
        if (p.getImages()!=null) d.setImages(toImageList(p.getImages()));
        return d;
    }
    public static ProductDto toDtoSummary(Product p){
        if (p==null) return null; ProductDto d=new ProductDto();
        d.setId(p.getId()); d.setName(p.getName()); d.setSlug(p.getSlug()); d.setBrand(p.getBrand());
        if (p.getCategory()!=null) d.setCategory(CategoryUtils.toDto(p.getCategory()));
        d.setPrice(p.getPrice()); d.setPriceOld(p.getPriceOld()); d.setDiscount(p.getDiscount());
        d.setThumbnailImage(p.getThumbnailImage()); d.setQuantity(p.getQuantity()); d.setIsPublished(p.getIsPublished()); d.setPublishedAt(p.getPublishedAt());
        d.setCreatedAt(p.getCreatedAt()); d.setUpdatedAt(p.getUpdatedAt());
        return d;
    }
    public static Page<ProductDto> toDtoPage(Page<Product> page){
        List<ProductDto> list = page.getContent().stream().map(ProductUtils::toDto).collect(Collectors.toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }
    public static Page<ProductDto> toDtoPageSummary(Page<Product> page){
        List<ProductDto> list = page.getContent().stream().map(ProductUtils::toDtoSummary).collect(Collectors.toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }

    public static ProductColorDto toDto(ProductColor c){
        if (c==null) return null; ProductColorDto d=new ProductColorDto();
        d.setId(c.getId()); d.setProductId(c.getProduct()!=null?c.getProduct().getId():null);
        d.setName(c.getName()); d.setHexColor(c.getHexColor());
        d.setCreatedAt(c.getCreatedAt()); d.setUpdatedAt(c.getUpdatedAt()); return d;
    }
    public static List<ProductColorDto> toColorList(List<ProductColor> list){ return list.stream().map(ProductUtils::toDto).collect(Collectors.toList()); }

    public static ProductImageDto toDto(ProductImage i){
        if (i==null) return null; ProductImageDto d=new ProductImageDto();
        d.setId(i.getId()); d.setProductId(i.getProduct()!=null?i.getProduct().getId():null);
        d.setUrl(i.getUrl()); d.setCreatedAt(i.getCreatedAt()); d.setUpdatedAt(i.getUpdatedAt()); return d;
    }
    public static List<ProductImageDto> toImageList(List<ProductImage> list){ return list.stream().map(ProductUtils::toDto).collect(Collectors.toList()); }

    public static ProductSpecificationDto toDto(ProductSpecification s){
        if (s==null) return null; ProductSpecificationDto d=new ProductSpecificationDto();
        d.setId(s.getId()); d.setProductId(s.getProduct()!=null?s.getProduct().getId():null);
        d.setGroupName(s.getGroupName()); d.setLabel(s.getLabel()); d.setValue(s.getValue()); d.setType(s.getType());
        d.setCreatedAt(s.getCreatedAt()); d.setUpdatedAt(s.getUpdatedAt()); return d;
    }
    public static List<ProductSpecificationDto> toSpecList(List<ProductSpecification> list){ return list.stream().map(ProductUtils::toDto).collect(Collectors.toList()); }
    
    public static ProductFavoriteDto toDto(ProductFavorite pf){
        if (pf==null) return null; ProductFavoriteDto d=new ProductFavoriteDto();
        d.setId(pf.getId()); d.setUserId(pf.getUser()!=null?pf.getUser().getId():null);
        d.setProductId(pf.getProduct()!=null?pf.getProduct().getId():null);
        if (pf.getUser()!=null) d.setUser(UserUtils.toDto(pf.getUser()));
        if (pf.getProduct()!=null) d.setProduct(toDtoSummary(pf.getProduct()));
        d.setCreatedAt(pf.getCreatedAt()); return d;
    }
    public static Page<ProductFavoriteDto> toFavoriteDtoPage(Page<ProductFavorite> page){
        List<ProductFavoriteDto> list = page.getContent().stream().map(ProductUtils::toDto).collect(Collectors.toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }
    
    public static ProductReviewDto toDto(ProductReview pr){
        if (pr==null) return null; ProductReviewDto d=new ProductReviewDto();
        d.setId(pr.getId()); d.setProductId(pr.getProduct()!=null?pr.getProduct().getId():null);
        d.setUserId(pr.getUser()!=null?pr.getUser().getId():null);
        d.setOrderId(pr.getOrder()!=null?pr.getOrder().getId():null);
        if (pr.getProduct()!=null) d.setProduct(toDtoSummary(pr.getProduct()));
        if (pr.getUser()!=null) d.setUser(UserUtils.toDto(pr.getUser()));
        d.setRating(pr.getRating()); d.setComment(pr.getComment());
        d.setCreatedAt(pr.getCreatedAt()); d.setUpdatedAt(pr.getUpdatedAt()); return d;
    }
    public static Page<ProductReviewDto> toReviewDtoPage(Page<ProductReview> page){
        List<ProductReviewDto> list = page.getContent().stream().map(ProductUtils::toDto).collect(Collectors.toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }
}



