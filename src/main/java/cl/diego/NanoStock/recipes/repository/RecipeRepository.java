package cl.diego.NanoStock.recipes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.recipes.model.Recipe;
import cl.diego.NanoStock.recipes.model.RecipeCategory;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByCategory(RecipeCategory category);

    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
}
