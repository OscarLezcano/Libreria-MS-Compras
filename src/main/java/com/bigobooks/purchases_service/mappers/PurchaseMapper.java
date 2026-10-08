package com.bigobooks.purchases_service.mappers;

import com.bigobooks.purchases_service.dto.PurchaseResponseDto;
import com.bigobooks.entities.purchases.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { PurchaseDetailMapper.class })
public interface PurchaseMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "details", source = "purchaseDetails")
    PurchaseResponseDto toResponse(Purchase purchase);

    List<PurchaseResponseDto> toResponseList(List<Purchase> purchases);
}
