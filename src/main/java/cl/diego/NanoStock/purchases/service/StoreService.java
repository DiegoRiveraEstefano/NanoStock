package cl.diego.NanoStock.purchases.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.purchases.dto.StoreRequest;
import cl.diego.NanoStock.purchases.dto.StoreResponse;
import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.model.StoreType;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        Store store = Store.builder()
            .name(request.getName().trim())
            .branchName(request.getBranchName())
            .storeType(request.getStoreType() != null ? request.getStoreType() : StoreType.SUPERMARKET)
            .address(request.getAddress())
            .build();

        return toStoreResponse(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
            .map(this::toStoreResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<StoreResponse> getStoreById(Long id) {
        return storeRepository.findById(id).map(this::toStoreResponse);
    }

    public StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
            .id(store.getId())
            .name(store.getName())
            .branchName(store.getBranchName())
            .storeType(store.getStoreType())
            .address(store.getAddress())
            .createdAt(store.getCreatedAt())
            .build();
    }
}
