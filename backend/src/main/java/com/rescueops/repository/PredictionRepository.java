package com.rescueops.repository;

import com.rescueops.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findTop50ByOrderByCreatedAtDesc();

    Optional<Prediction> findFirstByServiceNameIgnoreCaseOrderByCreatedAtDesc(String serviceName);
}
