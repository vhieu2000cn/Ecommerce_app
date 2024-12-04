package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {

	@Test
	public void contextLoads() {
	}

	@InjectMocks
	private UserController userController;

	@InjectMocks
	private ItemController itemController;

	@InjectMocks
	private CartController cartController;

	@InjectMocks
	private OrderController orderController;

	private final BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
	@Mock
	private CartRepository cartRepository = mock(CartRepository.class);
	@Mock
	private UserRepository userRepository = mock(UserRepository.class);
	@Mock
	private ItemRepository itemRepository = mock(ItemRepository.class);
	@Mock
	private OrderRepository orderRepository = mock(OrderRepository.class);

	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
	}

	public static List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			items.add(createItem(i));
		}
		return items;
	}

	public static Item createItem(long id) {
		Item item = new Item();
		item.setId(id);
		item.setPrice(BigDecimal.valueOf(123));
		item.setName("Item " + item.getId());
		item.setDescription("Description " + item.getId());
		return item;
	}

	public static Cart createCart(User user) {
		Cart cart = new Cart();
		cart.setId(123L);
		List<Item> items = createItems();
		cart.setUser(user);
		cart.setItems(createItems());
		cart.setTotal(items.stream().map(Item::getPrice).reduce(BigDecimal::add).get());
		return cart;
	}

	public static User createUser() {
		User user = new User();
		user.setId(1L);
		user.setUsername("username");
		user.setPassword("password");
		user.setCart(createCart(user));
		return user;
	}


	@Test
	public void create_user_success(){
		when(bCryptPasswordEncoder.encode("password")).thenReturn("password_hashed");

		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user_name");
		request.setPassword("password");
		ResponseEntity<User> response = userController.createUser(request);
		assertEquals(200, response.getStatusCodeValue());
		User user = response.getBody();
		assertNotNull(user);
		assertEquals(0, user.getId());
		assertEquals("user_name", user.getUsername());
		assertEquals("password_hashed", user.getPassword());
	}

	@Test
	public void create_user_exists(){
		when(bCryptPasswordEncoder.encode("password")).thenReturn("password_hashed");

		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user_name");
		request.setPassword("password");
		ResponseEntity<User> response = userController.createUser(request);
		assertEquals(200, response.getStatusCodeValue());
		User user = response.getBody();
		assertNotNull(user);
		assertEquals(0, user.getId());
		assertEquals("user_name", user.getUsername());
		assertEquals("password_hashed", user.getPassword());

		request.setUsername(user.getUsername());
		request.setPassword(user.getPassword());
		when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
		ResponseEntity<User> responseUserExist = userController.createUser(request);
		assertEquals(HttpStatus.BAD_REQUEST, responseUserExist.getStatusCode());
	}

	@Test
	public void get_all_item() {
		int itemSize = 10;
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < itemSize; i++) {
			items.add(createItem(i));
		}
		when(itemRepository.findAll()).thenReturn(items);
		ResponseEntity<List<Item>> response = itemController.getItems();
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<Item> actualItems = response.getBody();
		assertNotNull(actualItems);
		assertEquals(itemSize, actualItems.size());
	}

	@Test
	public void get_item_by_id() {
		Item item = createItem(5L);
		when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
		ResponseEntity<Item> response = itemController.getItemById(5L);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}


	@Test
	public void get_item_by_name() {
		Item item = createItem(5L);
		item.setName("item");
		List<Item> items = new ArrayList<>();
		items.add(item);
		when(itemRepository.findByName("item")).thenReturn(items);
		ResponseEntity<List<Item>> response = itemController.getItemsByName("item");
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("item", response.getBody().get(0).getName());
	}

	@Test
	public void add_to_cart() {
		ModifyCartRequest request = new ModifyCartRequest();
		request.setQuantity(1);
		request.setItemId(1);
		request.setUsername("username");

		User user = createUser();
		Item item = createItem(0L);
		when(userRepository.findByUsername("username")).thenReturn(user);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		ResponseEntity<Cart> response = cartController.addTocart(request);
		Cart actualCart = response.getBody();
		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(actualCart);
		assertEquals("username", actualCart.getUser().getUsername());
		assertEquals(item.getId(), actualCart.getItems().get(0).getId());
	}

	@Test
	public void remove_cart() {
		ModifyCartRequest request = new ModifyCartRequest();
		request.setQuantity(2);
		request.setItemId(1);
		request.setUsername("username");
		User user = createUser();
		Item item = createItem(0L);
		when(userRepository.findByUsername("username")).thenReturn(user);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		ResponseEntity<Cart> response = cartController.removeFromcart(request);
		Cart actualCart = response.getBody();
		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(actualCart);
		assertEquals("username", actualCart.getUser().getUsername());
	}

	@Test
	public void create_order() {
		User user = createUser();
		when(userRepository.findByUsername("username")).thenReturn(user);
		ResponseEntity<UserOrder> response = orderController.submit("username");
		UserOrder order = response.getBody();
		verify(userRepository, times(1)).findByUsername("username");
		verify(orderRepository, times(1)).save(order);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(createItem(0L).getId(), order.getItems().get(0).getId());
		assertEquals(createUser().getId(), order.getUser().getId());
	}

	@Test
	public void get_orders_by_user() {
		User user = createUser();
		UserOrder order = UserOrder.createFromCart(user.getCart());
		List<UserOrder> orders = new ArrayList<>();
		orders.add(order);
		when(userRepository.findByUsername(any())).thenReturn(user);
		when(orderRepository.findByUser(user)).thenReturn(orders);
		ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("username");
		List<UserOrder> actualOrders = response.getBody();
		verify(userRepository, times(1)).findByUsername("username");
		verify(orderRepository, times(1)).findByUser(user);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(createItem(0L), order.getItems().get(0));
		assertEquals(createUser().getId(), order.getUser().getId());


	}
}
