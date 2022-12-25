package org.sid.service;


import java.util.List;

import org.sid.dto.InventoryResponse;
import org.sid.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
    	return inventoryRepository.findBySkuCodeIn(skuCode).stream()
        		.map(inventory->
        			InventoryResponse.builder().skuCode(inventory.getSkuCode())
        			.isInStock(inventory.getQuantity()>0).build()
        		).toList();
    }
}
