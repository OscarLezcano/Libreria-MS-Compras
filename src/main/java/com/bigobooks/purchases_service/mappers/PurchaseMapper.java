package com.bigobooks.purchases_service.mappers;

import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.entities.purchases.Purchase;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { PurchaseDetailMapper.class })
public interface PurchaseMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "purchaseDetails", source = "purchaseDetails")
    PurchaseDto toResponse(Purchase purchase);

    List<PurchaseDto> toResponseList(List<Purchase> purchases);
}
