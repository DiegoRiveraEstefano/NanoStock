package cl.diego.NanoStock.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.dto.UpdateProductRequest;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogViewController {

    private final ProductCatalogService catalogService;
    private final InventoryService inventoryService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {

        List<ProductResponse> products;
        if (query != null && !query.trim().isEmpty()) {
            products = catalogService.searchProducts(query.trim());
        } else {
            products = catalogService.getAllProducts();
        }

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> categoryId.equals(p.getCategoryId()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", catalogService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("query", query);
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);

        return "pages/catalog/index";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("form", new CreateProductRequest());
        model.addAttribute("categories", catalogService.getAllCategories());
        model.addAttribute("brands", catalogService.getAllBrands());
        model.addAttribute("unitsOfMeasure", UnitOfMeasure.values());
        model.addAttribute("productId", null);
        return "pages/catalog/product_form";
    }

    @PostMapping
    public String createProduct(@ModelAttribute CreateProductRequest form, RedirectAttributes redirectAttributes) {
        try {
            ProductResponse created = catalogService.createProduct(form);
            redirectAttributes.addFlashAttribute("flashMessage", "Producto '" + created.getName() + "' registrado correctamente.");
            return "redirect:/catalog/" + created.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/catalog/new";
        }
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductResponse product = catalogService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        var stockOverview = inventoryService.getProductStockOverview(id);

        model.addAttribute("product", product);
        model.addAttribute("stockOverview", stockOverview);
        return "pages/catalog/product_detail";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        ProductResponse product = catalogService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        CreateProductRequest form = CreateProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .barCode(product.getBarCode())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .unitOfMeasure(product.getUnitOfMeasure())
                .minStockThreshold(product.getMinStockThreshold())
                .targetStockQuantity(product.getTargetStockQuantity())
                .isPerishable(product.isPerishable())
                .build();

        model.addAttribute("form", form);
        model.addAttribute("productId", id);
        model.addAttribute("categories", catalogService.getAllCategories());
        model.addAttribute("brands", catalogService.getAllBrands());
        model.addAttribute("unitsOfMeasure", UnitOfMeasure.values());

        return "pages/catalog/product_form";
    }

    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute UpdateProductRequest form,
            RedirectAttributes redirectAttributes) {
        try {
            catalogService.updateProduct(id, form);
            redirectAttributes.addFlashAttribute("flashMessage", "Producto actualizado correctamente.");
            return "redirect:/catalog/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/catalog/" + id + "/edit";
        }
    }
}
