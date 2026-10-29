package com.bigobooks.purchases_service.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PurchaseDetailRepository extends BaseRepository<PurchaseDetail> {

	@Query("select d from PurchaseDetail d where d.purchase.id = :purchaseId")
	List<PurchaseDetail> findByPurchaseId(@Param("purchaseId") Long purchaseId);

	@Override
	@Query(value = "SELECT * FROM purchase_detail", nativeQuery = true)
	List<PurchaseDetail> findAllIncludingDeleted();

	@Override
	@Query(value = "SELECT * FROM purchase_detail WHERE is_deleted = true", nativeQuery = true)
	List<PurchaseDetail> findDeleted();
}
