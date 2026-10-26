package com.bigobooks.purchases_service.mappers;

import com.bigobooks.dto.PurchaseDetailDto;
import com.bigobooks.entities.purchases.PurchaseDetail;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseDetailMapper {

    @Mapping(target = "purchaseId", source = "purchase.id")
    PurchaseDetailDto toResponse(PurchaseDetail detail);

    List<PurchaseDetailDto> toResponseList(List<PurchaseDetail> details);
}
