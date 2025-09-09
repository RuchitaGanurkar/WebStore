package com.webstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.request.cart.CartRequestDto;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.entity.cart.CartStatus;
import com.webstore.entity.order.OrderStatus;
import com.webstore.entity.product.*;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.enums.order.OrderStatusType;
import com.webstore.repository.cart.CartProductStatusRepository;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.repository.order.OrderStatusRepository;
import com.webstore.repository.product.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test for Cart to Order Conversion Flow
 * 
 * This test validates the complete business flow:
 * 1. Create Cart with phone number validation
 * 2. Add Product to Cart
 * 3. Verify Cart contains products
 * 4. Checkout Cart → Order conversion
 * 5. Verify Order details and total amount
 * 6. Verify Cart status changes to ARCHIVED
 * 7. Verify Order-Cart relationship
 * 
 * Built from scratch using codebase references for:
 * - Entity relationships and constraints
 * - DTO structures and validations
 * - Controller endpoints and responses
 * - Service layer business logic
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartToOrderE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // === Repository Dependencies for Test Data Setup ===
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CatalogueRepository catalogueRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductPriceRepository productPriceRepository;
    
    @Autowired
    private CartStatusRepository cartStatusRepository;
    
    @Autowired
    private CartProductStatusRepository cartProductStatusRepository;
    
    @Autowired
    private OrderStatusRepository orderStatusRepository;

    // === Test Data Variables ===
    private String testPhoneNumber = "9876543210";
    private Integer catalogueId;
    private Integer productId;
    private Integer currencyId;
    private Integer cartActiveStatusId;
    private Integer cartArchivedStatusId;
    private Integer cartProductAddedStatusId;
    private Integer orderPendingStatusId;
    private BigInteger productPriceAmount = BigInteger.valueOf(299900); // ₹2999.00

    @BeforeEach
    void setupTestData() {
        // === 1. Create Test User ===
        User user = new User();
        user.setPhoneNumber(testPhoneNumber);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole("CUSTOMER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // === 2. Create Test Currency ===
        Currency currency = new Currency();
        currency.setCurrencyCode("INR");
        currency.setCurrencyName("Indian Rupee");  // ✅ Fix: Add missing currency name
        currency.setCurrencySymbol("₹");
        currency.setCreatedAt(LocalDateTime.now());
        currency.setUpdatedAt(LocalDateTime.now());
        Currency savedCurrency = currencyRepository.save(currency);
        currencyId = savedCurrency.getCurrencyId();

        // === 3. Create Test Category ===
        Category category = new Category();
        category.setCategoryName("Electronics");
        category.setCategoryDescription("Electronic items for testing");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        Category savedCategory = categoryRepository.save(category);

        // === 4. Create Test Catalogue ===
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueName("Test E-Commerce Catalogue");
        catalogue.setCatalogueDescription("Test catalogue for E2E testing");
        catalogue.setCreatedAt(LocalDateTime.now());
        catalogue.setUpdatedAt(LocalDateTime.now());
        Catalogue savedCatalogue = catalogueRepository.save(catalogue);
        catalogueId = savedCatalogue.getCatalogueId();

        // === 5. Create Test Product ===
        Product product = new Product();
        product.setProductName("Test Smartphone");
        product.setProductDescription("High-end smartphone for E2E testing");
        product.setCategory(savedCategory);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        productId = savedProduct.getProductId();

        // === 6. Create Test Product Price ===
        ProductPrice productPrice = new ProductPrice();
        productPrice.setProduct(savedProduct);
        productPrice.setCurrency(savedCurrency);
        productPrice.setPriceAmount(productPriceAmount);
        productPrice.setCreatedAt(LocalDateTime.now());
        productPrice.setUpdatedAt(LocalDateTime.now());
        productPriceRepository.save(productPrice);

        // === 7. Create Cart Statuses ===
        // Active Status
        CartStatus activeStatus = new CartStatus();
        activeStatus.setStatusName(CartStatusType.ACTIVE);
        activeStatus.setCreatedAt(LocalDateTime.now());
        activeStatus.setUpdatedAt(LocalDateTime.now());
        CartStatus savedActiveStatus = cartStatusRepository.save(activeStatus);
        cartActiveStatusId = savedActiveStatus.getStatusId();

        // Archived Status
        CartStatus archivedStatus = new CartStatus();
        archivedStatus.setStatusName(CartStatusType.ARCHIVED);
        archivedStatus.setCreatedAt(LocalDateTime.now());
        archivedStatus.setUpdatedAt(LocalDateTime.now());
        CartStatus savedArchivedStatus = cartStatusRepository.save(archivedStatus);
        cartArchivedStatusId = savedArchivedStatus.getStatusId();

        // === 8. Create Cart Product Status ===
        CartProductStatus addedStatus = new CartProductStatus();
        addedStatus.setStatusName(CartProductStatusType.ADDED);
        addedStatus.setCreatedAt(LocalDateTime.now());
        addedStatus.setUpdatedAt(LocalDateTime.now());
        CartProductStatus savedAddedStatus = cartProductStatusRepository.save(addedStatus);
        cartProductAddedStatusId = savedAddedStatus.getStatusId();

        // === 9. Create Order Status ===
        OrderStatus pendingStatus = new OrderStatus();
        pendingStatus.setStatusName(OrderStatusType.PENDING);
        pendingStatus.setCreatedAt(LocalDateTime.now());
        pendingStatus.setUpdatedAt(LocalDateTime.now());
        OrderStatus savedPendingStatus = orderStatusRepository.save(pendingStatus);
        orderPendingStatusId = savedPendingStatus.getStatusId();
    }

    @Test
    void testCompleteCartToOrderFlow() throws Exception {
        System.out.println("🚀 Starting E2E Test: Cart → Order Conversion Flow");

        // ===== STEP 1: Create Cart =====
        System.out.println("📝 Step 1: Creating cart for phone number: " + testPhoneNumber);
        
        CartRequestDto cartRequest = new CartRequestDto();
        cartRequest.setPhoneNumber(Long.parseLong(testPhoneNumber));
        cartRequest.setCatalogueId(catalogueId);
        cartRequest.setStatusId(cartActiveStatusId);

        MvcResult cartResult = mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(Long.parseLong(testPhoneNumber)))
                .andExpect(jsonPath("$.catalogueId").value(catalogueId))
                .andExpect(jsonPath("$.statusId").value(cartActiveStatusId))
                .andExpect(jsonPath("$.cartId").exists())
                .andReturn();

        // Extract cart ID for subsequent operations
        Integer cartIdInt = JsonPath.read(cartResult.getResponse().getContentAsString(), "$.cartId");
        Long cartId = cartIdInt.longValue();
        System.out.println("✅ Cart created successfully with ID: " + cartId);

        // ===== STEP 2: Add Product to Cart =====
        System.out.println("🛒 Step 2: Adding product to cart");
        
        CartProductRequestDto productRequest = new CartProductRequestDto();
        productRequest.setCartId(cartId);
        productRequest.setProductId(Long.valueOf(productId));
        productRequest.setStatusId(cartProductAddedStatusId);
        productRequest.setQuantity(2);

        mockMvc.perform(post("/api/cart-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.statusId").value(cartProductAddedStatusId));

        System.out.println("✅ Product added to cart - ProductID: " + productId + ", Quantity: 2");

        // ===== STEP 3: Verify Cart Contains Products =====
        System.out.println("🔍 Step 3: Verifying cart contains products");
        
        mockMvc.perform(get("/api/cart-products/cart/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].productId").value(productId))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].cartId").value(cartId));

        System.out.println("✅ Cart verified to contain products");

        // ===== STEP 4: Checkout Cart → Order Conversion =====
        System.out.println("💳 Step 4: Checking out cart to create order");
        
        MvcResult orderResult = mockMvc.perform(post("/api/orders/checkout/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.statusName").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.orderId").exists())
                .andReturn();

        // Extract order details
        Integer orderIdInt = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.orderId");
        Long orderId = orderIdInt.longValue();
        Number totalAmountNumber = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.totalAmount");
        String totalAmount = totalAmountNumber.toString();

        System.out.println("✅ Order created successfully with ID: " + orderId);
        System.out.println("💰 Total Amount: ₹" + totalAmount);

        // ===== STEP 5: Verify Order Details =====
        System.out.println("📋 Step 5: Verifying order details");
        
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.statusName").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(totalAmount));

        System.out.println("✅ Order details verified successfully");

        // ===== STEP 6: Verify Cart Status Changed to ARCHIVED =====
        System.out.println("📦 Step 6: Verifying cart status changed to ARCHIVED");
        
        mockMvc.perform(get("/api/carts/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.statusId").value(cartArchivedStatusId));

        System.out.println("✅ Cart status verified as ARCHIVED");

        // ===== STEP 7: Verify Order-Cart Relationship =====
        System.out.println("🔗 Step 7: Verifying order-cart relationship");
        
        mockMvc.perform(get("/api/orders/cart/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(orderId))
                .andExpect(jsonPath("$[0].cartId").value(cartId));

        System.out.println("✅ Order-Cart relationship verified");
        System.out.println("🎉 E2E Test PASSED: Complete Cart → Order conversion successful!");
    }

    @Test
    void testCartCreationWithInvalidPhoneNumber() throws Exception {
        System.out.println("🚫 Testing cart creation with invalid phone number");
        
        CartRequestDto invalidCartRequest = new CartRequestDto();
        invalidCartRequest.setPhoneNumber(123L); // Invalid: less than 10 digits
        invalidCartRequest.setCatalogueId(catalogueId);
        invalidCartRequest.setStatusId(cartActiveStatusId);

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCartRequest)))
                .andExpect(status().isBadRequest());

        System.out.println("✅ Phone number validation working correctly");
    }

    @Test
    void testCheckoutEmptyCart() throws Exception {
        System.out.println("🚫 Testing checkout of empty cart");
        
        // Create cart without adding products
        CartRequestDto cartRequest = new CartRequestDto();
        cartRequest.setPhoneNumber(Long.parseLong(testPhoneNumber));
        cartRequest.setCatalogueId(catalogueId);
        cartRequest.setStatusId(cartActiveStatusId);

        MvcResult cartResult = mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Integer cartIdInt = JsonPath.read(cartResult.getResponse().getContentAsString(), "$.cartId");
        Long cartId = cartIdInt.longValue();

        // Attempt to checkout empty cart
        mockMvc.perform(post("/api/orders/checkout/" + cartId))
                .andExpect(status().isBadRequest());

        System.out.println("✅ Empty cart checkout validation working correctly");
    }

    @Test
    void testAddProductToNonExistentCart() throws Exception {
        System.out.println("🚫 Testing adding product to non-existent cart");
        
        CartProductRequestDto productRequest = new CartProductRequestDto();
        productRequest.setCartId(99999L); // Non-existent cart ID
        productRequest.setProductId(Long.valueOf(productId));
        productRequest.setStatusId(cartProductAddedStatusId);
        productRequest.setQuantity(1);

        mockMvc.perform(post("/api/cart-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound());

        System.out.println("✅ Non-existent cart validation working correctly");
    }
}
