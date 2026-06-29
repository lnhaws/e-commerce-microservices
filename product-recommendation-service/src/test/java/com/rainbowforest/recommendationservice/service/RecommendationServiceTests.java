package com.rainbowforest.recommendationservice.service;

import com.rainbowforest.recommendationservice.model.Product;
import com.rainbowforest.recommendationservice.model.Recommendation;
import com.rainbowforest.recommendationservice.model.User;
import com.rainbowforest.recommendationservice.repository.RecommendationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
// Đổi import sang anyLong
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RecommendationServiceTests {

    private final Long RECOMMENDATION_ID = 1L;
    private final Long PRODUCT_ID = 1L;
    private final Long USER_ID = 1L;
    private final Integer RATING = 5;
    private final String PRODUCT_NAME = "testProduct";
    private final String USER_NAME = "testUser";
    private User user;
    private Product product;
    private Recommendation recommendation;
    private List<Recommendation> recommendations;

    @Mock
    private RecommendationRepository repository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Before
    public void setUp(){
        user = new User();
        user.setUserName(USER_NAME);
        product = new Product();
        product.setProductName(PRODUCT_NAME);
        
        recommendation = new Recommendation();
        recommendation.setId(RECOMMENDATION_ID);
        recommendation.setUserId(USER_ID);       
        recommendation.setProductId(PRODUCT_ID); 
        recommendation.setRating(RATING);
        
        recommendations = new ArrayList<>();
        recommendations.add(recommendation);
    }

    @Test
    public void get_all_recommendation_by_product_id_test(){ // Đổi tên Test
        //given: Gọi hàm mới theo ID
        when(repository.findAllRatingByProductId(anyLong())).thenReturn(recommendations);

        //when: Gọi hàm mới theo ID
        List<Recommendation> foundRecommendations = recommendationService.getAllRecommendationByProductId(PRODUCT_ID);

        //then
        assertEquals(foundRecommendations.get(0).getId(), RECOMMENDATION_ID);
        assertEquals(foundRecommendations.get(0).getProductId(), PRODUCT_ID);
        assertEquals(foundRecommendations.get(0).getUserId(), USER_ID);
        
        Mockito.verify(repository, Mockito.times(1)).findAllRatingByProductId(anyLong());
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    public void save_recommendation_test(){
        //given
        when(repository.save(any(Recommendation.class))).thenReturn(recommendation);

        //when
        Recommendation found = recommendationService.saveRecommendation(recommendation);

        //then
        assertEquals(found.getId(), RECOMMENDATION_ID);
        assertEquals(found.getProductId(), PRODUCT_ID);
        assertEquals(found.getUserId(), USER_ID);
        
        Mockito.verify(repository, Mockito.times(1)).save(any(Recommendation.class));
        Mockito.verifyNoMoreInteractions(repository);
    }
}