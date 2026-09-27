package com.project.mycash.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryKasService {

    private final CategoryKasRepository categoryRepo;
    private final ActivityLogService logService;


    // =========================
    // CREATE
    // =========================

    public CategoryKas save(
            CategoryKas category,
            User user) {

        if (category.getName() == null
                || category.getName().isBlank()) {

            throw new RuntimeException(
                    "Nama kategori wajib diisi");
        }

        if (category.getAccountName() == null
                || category.getAccountName().isBlank()) {

            throw new RuntimeException(
                    "Akun jurnal wajib diisi");
        }

        String name =
                category.getName().trim();

        String accountName =
                category.getAccountName().trim();


        boolean isNew =
                category.getId() == null;


        // =========================
        // CHECK DUPLICATE
        // =========================

        boolean duplicate;

        if (isNew) {

            duplicate =
                    categoryRepo.existsByNameAndUser(
                            name,
                            user);

        } else {

            CategoryKas existing =
                    categoryRepo.findByIdAndUser(
                            category.getId(),
                            user);

            if (existing == null) {

                throw new RuntimeException(
                        "Kategori tidak ditemukan");
            }

            duplicate =
                    categoryRepo
                            .existsByNameAndUserAndIdNot(
                                    name,
                                    user,
                                    category.getId());
        }


        if (duplicate) {

            throw new RuntimeException(
                    "Kategori dengan nama tersebut sudah ada");
        }


        // =========================
        // OWNERSHIP
        // =========================

        category.setName(name);
        category.setAccountName(accountName);
        category.setUser(user);


        CategoryKas saved =
                categoryRepo.save(category);


        // =========================
        // ACTIVITY LOG
        // =========================

        logService.log(
                user,
                isNew ? "CREATE" : "UPDATE",
                (isNew
                        ? "Menambahkan kategori "
                        : "Mengubah kategori ")
                        + saved.getName()
        );


        return saved;
    }


    // =========================
    // READ
    // =========================

    public List<CategoryKas> findByUser(
            User user) {

        return categoryRepo.findByUser(user);
    }


    public CategoryKas findById(
            Long id,
            User user) {

        CategoryKas category =
                categoryRepo.findByIdAndUser(
                        id,
                        user);

        if (category == null) {

            throw new RuntimeException(
                    "Kategori tidak ditemukan");
        }

        return category;
    }


    // =========================
    // DELETE
    // =========================

    public void delete(
            Long id,
            User user) {

        CategoryKas category =
                categoryRepo.findByIdAndUser(
                        id,
                        user);

        if (category == null) {

            throw new RuntimeException(
                    "Kategori tidak ditemukan");
        }


        categoryRepo.delete(category);


        logService.log(
                user,
                "DELETE",
                "Menghapus kategori "
                        + category.getName()
        );
    }
}