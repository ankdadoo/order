package com.ewolff.microservice.order.logic;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ewolff.microservice.order.clients.CatalogClient;
import com.ewolff.microservice.order.clients.Customer;
import com.ewolff.microservice.order.clients.CustomerClient;
import com.ewolff.microservice.order.clients.Item;
import com.ewolff.microservice.order.clients.PaymentClient;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Controller
class OrderController {

	private OrderRepository orderRepository;

	private OrderService orderService;

	private CustomerClient customerClient;
	private CatalogClient catalogClient;

	private final static String QUEUE_NAME = "hello";
	//private PaymentClient paymentClient;
	
	@Autowired
	private OrderController(OrderService orderService,
			OrderRepository orderRepository, CustomerClient customerClient,
			CatalogClient catalogClient ) {
		super();
		this.orderRepository = orderRepository;
		this.customerClient = customerClient;
		this.catalogClient = catalogClient;
		this.orderService = orderService;
		//this.paymentClient = paymentClient;
	}

	@ModelAttribute("items")
	public Collection<Item> items() throws Exception{
		System.out.println("**** getting items -- calling payment client method  ");
		System.out.println ("Sending message to rabbit mq ");
		
		return catalogClient.findAll();
		
	}

	@ModelAttribute("customers")
	public Collection<Customer> customers() {
		System.out.println ("in the get customers ");
		return customerClient.findAll();
	}

	@RequestMapping("/ping")
	public String ping() {
		System.out.println("**** in id.html-- 2");
		return "Pinging -- order MS ";
	}
	@RequestMapping("/orders")
	public ModelAndView orderList() {
		System.out.println("in the get order list method ");
		return new ModelAndView("orderlist", "orders",
				orderRepository.findAll());
	}

	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public ModelAndView form() {
		return new ModelAndView("orderForm", "order", new Order());
	}

	@RequestMapping(value = "/line", method = RequestMethod.POST)
	public ModelAndView addLine(Order order) {
		order.addLine(0, catalogClient.findAll().iterator().next().getItemId());
		return new ModelAndView("orderForm", "order", order);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ModelAndView get(@PathVariable("id") long id) {
		return new ModelAndView("order", "order", orderRepository.findOne(id));
	}

	@RequestMapping(value = "/orders", method = RequestMethod.POST)
	public ModelAndView post(Order order) {
		System.out.println("**** saving order ");
		order = orderService.order(order);
		return new ModelAndView("success");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ModelAndView post(@PathVariable("id") long id) {
		orderRepository.delete(id);

		return new ModelAndView("success");
	}

}
