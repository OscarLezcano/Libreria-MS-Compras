package com.bigobooks.purchases_service.repository;

import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseDetailRepository extends BaseRepository<PurchaseDetail> {

	@Override
	@Query(value = "SELECT * FROM purchase_detail", nativeQuery = true)
	List<PurchaseDetail> findAllIncludingDeleted();

	@Override
	@Query(value = "SELECT * FROM purchase_detail WHERE is_deleted = true", nativeQuery = true)
	List<PurchaseDetail> findDeleted();
}