package com.rainbowforest.recommendationservice.service;

import com.rainbowforest.recommendationservice.model.Recommendation;
import java.util.List;

public interface RecommendationService {
    Recommendation getRecommendationById(Long recommendationId);

    Recommendation saveRecommendation(Recommendation recommendation);

    List<Recommendation> getAllRecommendationByProductId(Long productId);

    void deleteRecommendation(Long id);
}
