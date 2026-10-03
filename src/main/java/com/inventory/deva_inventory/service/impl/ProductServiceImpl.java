/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.BrandRepository;
import com.inventory.deva_inventory.dao.CategoryRepository;
import com.inventory.deva_inventory.dao.InventoryRepository;
import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.ProductRepository;
import com.inventory.deva_inventory.dao.SuppliedProductRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.model.Brand;
import com.inventory.deva_inventory.model.Category;
import com.inventory.deva_inventory.model.Inventory;
import com.inventory.deva_inventory.model.Product;
import com.inventory.deva_inventory.model.SuppliedProduct;
import com.inventory.deva_inventory.model.Supplier;
import com.inventory.deva_inventory.service.ProductService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author best
 */
@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private SupplierRepository supplierRepo;
    @Autowired
    private SuppliedProductRepository supProductRepository;
    @Autowired
    private CategoryRepository catRepo;
    @Autowired
    private BrandRepository brandRepo;
    @Autowired
    private InventoryRepository invRepo;

    @Transactional
    @Override
    public Product saveProduct(Integer supplierId, Integer suppliedProductId,
            Integer categoryId, Integer brandId,Product  product) {
        SuppliedProduct suppliedProduct = supProductRepository.findById(suppliedProductId)
                .orElseThrow(() -> new ResourceNotFoundException("SuppliedProduct", "id", suppliedProductId));
        Category cat = catRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        Brand brand = brandRepo.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        Supplier sup = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));

        suppliedProduct.setSuppliedProductStatus("recieved");
        suppliedProduct = supProductRepository.save(suppliedProduct);
        product.setSupProduct(suppliedProduct);
        product.setRecievedDate(new Date());
        product.setSupplier(sup);
        product.setCategory(cat);
        product.setBrand(brand);
        product.setStockStatus("un-stocked");
        return productRepo.save(product);
    }

    @Override
    public void deleteProduct(Integer productId) {
        if (!productRepo.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        productRepo.deleteById(productId);
    }

    @Override
    public Product editProduct(Integer productId, Product product) {
        Product prod = findProduct(productId);
        prod.setProductNumber(product.getProductNumber());
        prod.setProductName(product.getProductName());
        prod.setProductQuantity(product.getProductQuantity());
        prod.setProductPrice(product.getProductPrice());
        prod.setRecievedDate(new Date());
        prod.setExpiryDate(product.getExpiryDate());
        return productRepo.save(prod);
    }

    @Override
    public List<Product> listAllProduct() {
        return productRepo.findAll();
    }

    @Override
    public List<Product> listAllProductById(Integer orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        return productRepo.getAllProductByOrderId(orderId);
    }

    @Override
    public List<Product> listAllProductByCategoryId(Integer categoryId) {
        if (!catRepo.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        return productRepo.getAllProductByCategory(categoryId);
    }

    @Override
    public List<Product> listAllProductByBrandId(Integer brandId) {
        if (!brandRepo.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand", "id", brandId);
        }
        return productRepo.getAllProductByBrand(brandId);
    }

    @Override
    public Product getProductByProductNumber(String productNumber) {
        Product pro = productRepo.getProductByProductNumber(productNumber);
        if (pro == null) {
            throw new ResourceNotFoundException("Product", "productNumber", productNumber);
        }
        return pro;
    }

    @Override
    public List<Product> listAllProductByProductStockStatus(String stockStatus) {
        return productRepo.getAllProductByStockStatus(stockStatus);
    }

    @Override
    public Product addProductToInventory(Integer productId, String inventoryCode) {
        Inventory inv = invRepo.getInventoryByInventoryCode(inventoryCode);
        if (inv == null) {
            throw new ResourceNotFoundException("Inventory", "inventoryCode", inventoryCode);
        }
        Product pro = findProduct(productId);
        pro.setInventory(inv);
        pro.setStockStatus("stocked");
        return productRepo.save(pro);
    }

    @Override
    public List<Product> listProductByInventory(Integer inventoryId) {
        if (!invRepo.existsById(inventoryId)) {
            throw new ResourceNotFoundException("Inventory", "id", inventoryId);
        }
        return productRepo.getAllProductByInventory(inventoryId);
    }

    private Product findProduct(Integer productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

}
