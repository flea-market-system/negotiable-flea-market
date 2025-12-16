package com.example.negotiable_flea_market.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.negotiable_flea_market.service.ItemService;
import com.example.negotiable_flea_market.entity.Item;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 商品一覧表示
    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        return "items/list"; // templates/items/list.html を想定
    }

    // 商品詳細表示
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Item item = itemService.getItemById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        model.addAttribute("item", item);
        return "item_detail"; // templates/item_detail.html を想定
    }
}
