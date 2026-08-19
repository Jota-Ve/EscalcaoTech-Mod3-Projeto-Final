package com.ada.transferscheduling.repository;

import com.ada.transferscheduling.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
