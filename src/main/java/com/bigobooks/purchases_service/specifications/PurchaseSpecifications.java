package com.bigobooks.purchases_service.specifications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.bigobooks.entities.purchases.Purchase;
import com.bigobooks.entities.purchases.PurchaseDetail;
import com.bigobooks.entities.purchases.PurchaseStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Filtros paginados de compras.
 */
public final class PurchaseSpecifications {

	private PurchaseSpecifications() {
	}

	public static Specification<Purchase> build(PurchaseStatus status, Long warehouseId, Long supplierId, Long bookId,
			String invoiceNumber, LocalDateTime createdFrom, LocalDateTime createdTo) {
		return (Root<Purchase> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.isFalse(root.get("isDeleted")));

			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (warehouseId != null) {
				predicates.add(cb.equal(root.get("warehouseId"), warehouseId));
			}
			if (supplierId != null) {
				Join<Object, Object> supplier = root.join("supplier");
				predicates.add(cb.equal(supplier.get("id"), supplierId));
			}
			if (bookId != null) {
				Subquery<Long> subquery = query.subquery(Long.class);
				Root<PurchaseDetail> detail = subquery.from(PurchaseDetail.class);
				subquery.select(detail.get("id"))
						.where(cb.equal(detail.get("purchase"), root), cb.equal(detail.get("bookId"), bookId));
				predicates.add(cb.exists(subquery));
			}
			if (invoiceNumber != null && !invoiceNumber.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("invoiceNumber")),
						"%" + invoiceNumber.trim().toLowerCase() + "%"));
			}
			if (createdFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
			}
			if (createdTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
