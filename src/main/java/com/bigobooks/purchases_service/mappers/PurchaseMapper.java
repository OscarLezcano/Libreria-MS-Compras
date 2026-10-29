package com.bigobooks.purchases_service.mappers;

import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.entities.purchases.PurchaseDetail;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "detailIds", source = "purchaseDetails")
    PurchaseDto toResponse(Purchase purchase);

    List<PurchaseDto> toResponseList(List<Purchase> purchases);

    @SuppressWarnings("null")
    default List<Long> toDetailIds(List<PurchaseDetail> details) {
        return details == null ? new ArrayList<>()
                : details.stream().map(PurchaseDetail::getId).toList();
    }
}
