package com.bigobooks.purchases_service.mappers;

import com.bigobooks.dto.SupplierDto;
import com.bigobooks.entities.purchases.Supplier;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierDto toResponse(Supplier supplier);

    List<SupplierDto> toResponseList(List<Supplier> suppliers);
}