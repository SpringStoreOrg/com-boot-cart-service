
package com.boot.cart.model;

import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.ProductDTO;
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

import static com.boot.cart.model.CartEntryMapper.cartEntityToDtoList;


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
	@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY,  cascade = { CascadeType.ALL}, orphanRemoval = true)
	private List<CartEntry> entries;

	@PreUpdate
	protected void lastUpdatedOnPreUpdate() {
		this.lastUpdatedOn =  LocalDateTime.now();
	}


	public static CartDTO cartEntityToDto(Cart cart, List<ProductDTO> productsInCart) {
		return new CartDTO()
				.setId(cart.getId())
				.setUserId(cart.getUserId())
				.setEntries(cartEntityToDtoList(cart, productsInCart))
				.setTotal(cart.getTotal());
	}
}
