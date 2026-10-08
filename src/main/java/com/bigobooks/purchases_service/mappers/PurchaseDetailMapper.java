package com.bigobooks.purchases_service.mappers;

import com.bigobooks.purchases_service.dto.PurchaseDetailResponseDto;
import com.bigobooks.entities.purchases.PurchaseDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseDetailMapper {

    @Mapping(target = "lineTotal", expression = "java((long) detail.getQuantity() * detail.getUnitCost())")
    PurchaseDetailResponseDto toResponse(PurchaseDetail detail);

    List<PurchaseDetailResponseDto> toResponseList(List<PurchaseDetail> details);
}
