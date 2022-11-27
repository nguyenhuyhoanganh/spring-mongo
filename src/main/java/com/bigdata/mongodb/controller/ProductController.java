package com.bigdata.mongodb.controller;

import com.bigdata.mongodb.entity.Brand;
import com.bigdata.mongodb.entity.Category;
import com.bigdata.mongodb.entity.Product;
import com.bigdata.mongodb.repository.BrandRepository;
import com.bigdata.mongodb.repository.CategoryRepository;
import com.bigdata.mongodb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/products")
@Transactional
public class ProductController {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ProductRepository repo;
    @Autowired
    private CategoryRepository cateRepo;
    @Autowired
    private BrandRepository brandRepo;

    @GetMapping("")
    public List<Product> findProducts(@RequestParam(value = "search", required = false) String search, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit, @RequestParam(value = "sortAscBy", required = false) String sortAscBy, @RequestParam(value = "sortDescBy", required = false) String sortDescBy) throws NoSuchFieldException {

        if (search != null) search = search.replaceAll("\\s\\s+", " ").trim();
        page = page == null ? 0 : page;
        limit = limit == null ? 10 : limit;
        sortAscBy = sortAscBy == null ? "id" : sortAscBy.replaceAll("\\s\\s+", " ").trim();

        List<Criteria> criteriaList = new ArrayList<>();
        Pattern patternSearch = Pattern.compile("(\\w+?)([:<>])(\\w+( +\\w+)*$?),", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcherSearch = patternSearch.matcher(search + ",");
        while (matcherSearch.find()) {
            System.out.println(matcherSearch.group(1) + matcherSearch.group(2) + " " + matcherSearch.group(3));

            if (matcherSearch.group(2).equalsIgnoreCase(":")) {
                if (matcherSearch.group(1).equals("keyword"))
                    criteriaList.add(new Criteria().orOperator(Arrays.asList(
                            Criteria.where("name").regex(".*" + matcherSearch.group(3) + ".*", "i"),
                            Criteria.where("category").in(cateRepo.findByNameContainingIgnoreCase(matcherSearch.group(3))),
                            Criteria.where("brand").in(brandRepo.findByNameContainingIgnoreCase(matcherSearch.group(3)))
                    )));
                else if (Product.class.getDeclaredField(matcherSearch.group(1)).getGenericType() == String.class)
                    criteriaList.add(Criteria.where(matcherSearch.group(1)).regex(".*" + matcherSearch.group(3) + ".*", "i"));
                else if (Product.class.getDeclaredField(matcherSearch.group(1)).getGenericType() == Category.class)
                    criteriaList.add(Criteria.where(matcherSearch.group(1)).in(cateRepo.findByNameContainingIgnoreCase(matcherSearch.group(3))));
                else if (Product.class.getDeclaredField(matcherSearch.group(1)).getGenericType() == Brand.class)
                    criteriaList.add(Criteria.where(matcherSearch.group(1)).in(brandRepo.findByNameContainingIgnoreCase(matcherSearch.group(3))));
                else
                    criteriaList.add(Criteria.where(matcherSearch.group(1)).is(Long.parseLong(matcherSearch.group(3))));
            }

            if (matcherSearch.group(2).equalsIgnoreCase(">"))
                criteriaList.add(Criteria.where(matcherSearch.group(1)).gte(Long.parseLong(matcherSearch.group(3))));

            if (matcherSearch.group(2).equalsIgnoreCase("<"))
                criteriaList.add(Criteria.where(matcherSearch.group(1)).lte(Long.parseLong(matcherSearch.group(3))));
        }

        Pattern patternSortProperty = Pattern.compile("(\\w+?),");

        Set<String> sortAscProperties = new HashSet<>();
        Matcher matcherSortProperty = patternSortProperty.matcher(sortAscBy + ",");
        while (matcherSortProperty.find()) {
            sortAscProperties.add(matcherSortProperty.group(1));
        }

        Set<String> sortDescProperties = new HashSet<>();
        matcherSortProperty = patternSortProperty.matcher(sortDescBy + ",");
        while (matcherSortProperty.find()) {
            sortDescProperties.add(matcherSortProperty.group(1));
        }

        Sort sort = Sort.by(sortAscProperties.stream().map(Sort.Order::asc).toList())
                .and(Sort.by(sortDescProperties.stream().map(Sort.Order::desc).toList()));

        Pageable pageable = PageRequest.of(page, limit, sort);

        Query query = new Query().with(pageable);

        if (!criteriaList.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        return mongoTemplate.find(query, Product.class);
    }

    @GetMapping("/{id}")
    public Product findProductById(@PathVariable String id) {
        return repo.findById(id).get();
    }

    @PutMapping("/update/{id}")
    public void updateProduct(@PathVariable String id, @RequestBody Product product) {
        if (repo.findById(id).isPresent()) {
            product.setId(id);
            if (product.getCategory() != null && cateRepo.findById(product.getCategory().getId()).isPresent())
                product.setCategory(cateRepo.findById(product.getCategory().getId()).get());
            else product.setCategory(null);
            if (product.getBrand() != null && brandRepo.findById(product.getBrand().getId()).isPresent())
                product.setBrand(brandRepo.findById(product.getBrand().getId()).get());
            else product.setBrand(null);
            repo.save(product);
        }
    }

    @PostMapping("/add")
    public void addProduct(@RequestBody Product product) {
        if (product.getCategory() != null && cateRepo.findById(product.getCategory().getId()).isPresent())
            product.setCategory(cateRepo.findById(product.getCategory().getId()).get());
        else product.setCategory(null);
        if (product.getBrand() != null && brandRepo.findById(product.getBrand().getId()).isPresent())
            product.setBrand(brandRepo.findById(product.getBrand().getId()).get());
        else product.setBrand(null);
        repo.save(product);
    }

    @DeleteMapping("/delete/{id}")
    public void removeProduct(@PathVariable String id) {
        if (repo.findById(id).isPresent()) repo.deleteById(id);
    }
}
