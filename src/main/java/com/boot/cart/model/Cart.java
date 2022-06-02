
package com.boot.cart.model;

import com.boot.cart.dto.CartDTO;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "cart")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "id")
public class Cart implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2904101271253876784L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	private Long userId;

	@Column
	private double total;

	@Column
	private LocalDateTime lastUpdatedOn;

	@JsonManagedReference
	@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY,  cascade = { CascadeType.ALL} )
	private List<CartEntry> entries;

	@PreUpdate
	protected void lastUpdatedOnPreUpdate() {
		this.lastUpdatedOn =  LocalDateTime.now();
	}


	public static CartDTO cartEntityToDto(Cart cart) {
		return new CartDTO()
				.setId(cart.getId())
				.setUserId(cart.getUserId())
				.setEntries(cart.getEntries())
				.setTotal(cart.getTotal());
	}

	public static Cart dtoToCartEntity(CartDTO cartDto) {
		return new Cart()
				.setId(cartDto.getId())
				.setUserId(cartDto.getUserId())
				.setEntries(cartDto.getEntries())
				.setTotal(cartDto.getTotal());
	}

	public static Cart updateDtoToCartEntity(Cart cart, CartDTO cartDto) {
		return cart.setId(cartDto.getId())
				.setUserId(cartDto.getUserId())
				.setEntries(cartDto.getEntries())
				.setTotal(cartDto.getTotal());
	}

	public static Set<CartDTO> cartEntityToDtoList(List<Cart> cartList) {

		Set<CartDTO> cartDTOList = new HashSet<>();

		cartList.stream().forEach(c -> cartDTOList.add(cartEntityToDto(c)));

		return cartDTOList;
	}

	public static Set<Cart> dtoToCartEntityList(List<CartDTO> cartDTOList) {

		Set<Cart> cartList = new HashSet<>();

		cartDTOList.stream().forEach(cDTO -> cartList.add(dtoToCartEntity(cDTO)));

		return cartList;
	}
}
