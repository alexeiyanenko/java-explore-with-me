package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryRequestDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryRequestDto categoryRequestDto);

    CategoryDto updateCategory(CategoryRequestDto categoryRequestDto, Long categoryId);

    void delete(Long categoryId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long categoryId);

}
