package mercadhar.service;

import mercadhar.exception.ResourceNotFoundException;
import mercadhar.model.Category;
import mercadhar.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
    }

    public Category create(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category already exists: " + name);
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    public Category update(Long id, String name, String description) {
        Category category = findById(id);
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        findById(id);
        categoryRepository.deleteById(id);
    }
}
