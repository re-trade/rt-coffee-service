package org.retrade.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.model.dto.request.BrandRequest;
import org.retrade.main.model.dto.response.BrandResponse;
import org.retrade.main.model.entity.BrandEntity;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.repository.jpa.BrandRepository;
import org.retrade.main.repository.jpa.CategoryRepository;
import org.retrade.main.service.impl.BrandServiceImpl;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {
    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    private BrandEntity brandEntity;
    private BrandRequest brandRequest;
    private Set<CategoryEntity> categories;

    @BeforeEach
    void setUp() {
        // Khởi tạo BrandEntity (không gán id vì từ BaseSQLEntity)
        brandEntity = new BrandEntity();
        brandEntity.setName("Test Brand");
        brandEntity.setDescription("Test Description");
        brandEntity.setImgUrl("http://image.url");
        brandEntity.setEnabled(true);

        // Khởi tạo BrandRequest
        brandRequest = new BrandRequest();
        brandRequest.setName("Test Brand");
        brandRequest.setDescription("Test Description");
        brandRequest.setImgUrl("http://image.url");
        brandRequest.setCategoryIds(new HashSet<>(Arrays.asList("cat1", "cat2")));

        // Khởi tạo danh sách CategoryEntity thủ công (không dùng builder)
        categories = new HashSet<>();
        CategoryEntity cat1 = new CategoryEntity();
        cat1.setId("cat1");
        cat1.setName("Category 1");
        cat1.setVisible(true);
        cat1.setEnabled(true);

        CategoryEntity cat2 = new CategoryEntity();
        cat2.setId("cat2");
        cat2.setName("Category 2");
        cat2.setVisible(true);
        cat2.setEnabled(true);

        categories.add(cat1);
        categories.add(cat2);

        // Mock các phương thức, giả định id được gán sau khi save
        /*when(categoryRepository.findAllById(anySet())).thenReturn(new ArrayList<>(categories));
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity savedEntity = invocation.getArgument(0);
            savedEntity.setId("1"); // Giả định ID được gán tự động
            return savedEntity;
        });
        when(brandRepository.findById("1")).thenReturn(Optional.of(brandEntity));
        when(brandRepository.createDefaultPredicate(any(CriteriaBuilder.class), any(Root.class), anyMap()))
                .thenReturn(new Predicate[]{mock(Predicate.class)});*/
    }
    private void setEntityId(Object entity, String id) throws Exception {
        Field idField = BaseSQLEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    // Test cho createBrand
    @Test
    void createBrand_Success() {
        // Mock các tương tác cần thiết
        when(categoryRepository.findAllById(anyIterable())).thenReturn(new ArrayList<>(categories));
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity savedEntity = invocation.getArgument(0);
            savedEntity.setId("1"); // Giả định ID được gán tự động
            return savedEntity;
        });

        BrandResponse result = brandService.createBrand(brandRequest);

        assertNotNull(result);
        assertEquals("Test Brand", result.getName());
        assertEquals("http://image.url", result.getImgUrl());
        assertEquals("1", result.getId());
        verify(categoryRepository).findAllById(anyIterable());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    void createBrand_Failed_Exception() {
        when(brandRepository.save(any(BrandEntity.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ActionFailedException.class, () -> brandService.createBrand(brandRequest),
                "Phải ném ActionFailedException khi save thất bại");
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    void createBrand_WithEmptyCategories() throws Exception {
        BrandRequest emptyCategoryRequest = new BrandRequest();
        emptyCategoryRequest.setName("Empty Category Brand");
        emptyCategoryRequest.setImgUrl("http://new.url");
        emptyCategoryRequest.setCategoryIds(Collections.emptySet());

        when(categoryRepository.findAllById(anySet())).thenReturn(Collections.emptyList());

        when(brandRepository.save(any(BrandEntity.class)))
                .thenAnswer(invocation -> {
                    BrandEntity saved = invocation.getArgument(0);
                    Field idField = BaseSQLEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(saved, "1");
                    return saved;
                });

        BrandResponse result = brandService.createBrand(emptyCategoryRequest);

        assertNotNull(result);
        assertEquals("Empty Category Brand", result.getName());
        assertEquals("http://new.url", result.getImgUrl());
        assertEquals("1", result.getId());
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }


    @Test
    void createBrand_WithNullRequest() {
        assertThrows(NullPointerException.class, () -> brandService.createBrand(null),
                "Phải ném NullPointerException khi request là null");
        verifyNoInteractions(categoryRepository, brandRepository);
    }

    @Test
    void createBrand_WithPartialData() throws Exception {
        BrandRequest partialRequest = new BrandRequest();
        partialRequest.setName("Partial Brand");
        partialRequest.setCategoryIds(new HashSet<>(Arrays.asList("cat1")));

        // Mock category
        when(categoryRepository.findAllById(anySet()))
                .thenReturn(List.of(categories.iterator().next()));

        // Mock save trả về entity có ID bằng reflection
        when(brandRepository.save(any(BrandEntity.class)))
                .thenAnswer(invocation -> {
                    BrandEntity saved = invocation.getArgument(0);
                    Field idField = BaseSQLEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(saved, "1");
                    return saved;
                });

        BrandResponse result = brandService.createBrand(partialRequest);

        assertNotNull(result);
        assertEquals("Partial Brand", result.getName());
        assertNull(result.getImgUrl()); // Không set imgUrl
        assertEquals("1", result.getId());

        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    // Test cho updateBrand
    @Test
    void updateBrand_Success() {
        BrandEntity mockEntity = mock(BrandEntity.class);
        when(mockEntity.getId()).thenReturn("1");
        when(mockEntity.getName()).thenReturn("Test Brand");
        when(mockEntity.getImgUrl()).thenReturn("http://image.url");

        when(brandRepository.findById("1")).thenReturn(Optional.of(mockEntity));
        when(categoryRepository.findAllById(anySet())).thenReturn(new ArrayList<>(categories));
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse result = brandService.updateBrand("1", brandRequest);

        assertNotNull(result);
        assertEquals("Test Brand", result.getName());
        assertEquals("http://image.url", result.getImgUrl());
        assertEquals("1", result.getId());

        verify(brandRepository).findById("1");
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }




    @Test
    void updateBrand_Failed_NotFound() {
        when(brandRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ActionFailedException.class, () -> brandService.updateBrand("nonexistent", brandRequest),
                "Phải ném ActionFailedException khi brand không tồn tại");
        verify(brandRepository).findById("nonexistent");
        verifyNoInteractions(categoryRepository);
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void updateBrand_Failed_Exception() {
        when(brandRepository.findById("1")).thenReturn(Optional.of(brandEntity));
        when(brandRepository.save(any(BrandEntity.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ActionFailedException.class, () -> brandService.updateBrand("1", brandRequest),
                "Phải ném ActionFailedException khi save thất bại");
        verify(brandRepository).findById("1");
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }
    @Test
    void updateBrand_WithEmptyCategories() throws Exception {
        BrandRequest emptyCategoryRequest = new BrandRequest();
        emptyCategoryRequest.setName("Updated Brand");
        emptyCategoryRequest.setImgUrl("http://updated.url");
        emptyCategoryRequest.setCategoryIds(Collections.emptySet());

        // Mock category list rỗng
        when(categoryRepository.findAllById(anySet())).thenReturn(Collections.emptyList());

        // Gán id cho brandEntity được trả về bởi findById
        Field idField = BaseSQLEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(brandEntity, "1");

        when(brandRepository.findById("1")).thenReturn(Optional.of(brandEntity));

        // Gán lại id cho entity khi save (vì nó được clone/copy lại)
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity saved = invocation.getArgument(0);
            idField.set(saved, "1");
            return saved;
        });

        BrandResponse result = brandService.updateBrand("1", emptyCategoryRequest);

        assertNotNull(result);
        assertEquals("Updated Brand", result.getName());
        assertEquals("http://updated.url", result.getImgUrl());
        assertEquals("1", result.getId());

        verify(brandRepository).findById("1");
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    void updateBrand_WithNullRequest() {
        when(brandRepository.findById("1")).thenReturn(Optional.of(brandEntity));

        assertThrows(NullPointerException.class, () -> brandService.updateBrand("1", null),
                "Phải ném NullPointerException khi request là null");
        verify(brandRepository).findById("1");
        verifyNoInteractions(categoryRepository);
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void updateBrand_WithSameData() throws Exception {
        // Gán ID cho brandEntity (vì nó là entity lấy từ repository)
        Field idField = BaseSQLEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(brandEntity, "1");

        when(brandRepository.findById("1")).thenReturn(Optional.of(brandEntity));

        // Gán ID cho entity khi save (vì entity mới sẽ được save lại)
        when(brandRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> {
            BrandEntity saved = invocation.getArgument(0);
            idField.set(saved, "1");
            return saved;
        });

        BrandResponse result = brandService.updateBrand("1", brandRequest);

        assertNotNull(result);
        assertEquals("Test Brand", result.getName());
        assertEquals("http://image.url", result.getImgUrl());
        assertEquals("1", result.getId());
        verify(brandRepository).findById("1");
        verify(categoryRepository).findAllById(anySet());
        verify(brandRepository).save(any(BrandEntity.class));
    }
}