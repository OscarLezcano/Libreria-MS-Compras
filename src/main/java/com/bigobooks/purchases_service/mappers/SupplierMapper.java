package com.bigobooks.purchases_service.mappers;

import com.bigobooks.purchases_service.dto.SupplierResponseDto;
import com.bigobooks.entities.purchases.Supplier;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponseDto toResponse(Supplier supplier);

    List<SupplierResponseDto> toResponseList(List<Supplier> suppliers);
}