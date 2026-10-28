package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockTransactionResponse;
import com.electricalwarehouse.entity.StockTransaction;
import com.electricalwarehouse.entity.StorageLocation;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.StockTransactionRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockTransactionServiceImpl implements StockTransactionService {

    private final StockTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DTOMapper mapper;

    @Override
    @Transactional
    public StockTransaction record(StockMovement m, StorageLocation location, String lotNumber, int signedQuantity,
                                   int balanceAfter) {
        StockTransaction tx = new StockTransaction();
        tx.setType(m.type());
        tx.setWarehouse(m.warehouse());
        tx.setProduct(m.product());
        tx.setLocation(location);
        tx.setLotNumber(lotNumber);
        tx.setQuantity(signedQuantity);
        tx.setBalanceAfter(balanceAfter);
        tx.setUnitCost(m.unitCost() != null ? m.unitCost() : m.product().getCostPrice());
        tx.setReferenceType(m.referenceType());
        tx.setReferenceId(m.referenceId());
        tx.setReferenceCode(m.referenceCode());
        tx.setNote(m.note());
        UserPrincipal user = CurrentUser.orNull();
        if (user != null) {
            tx.setPerformedBy(userRepository.getReferenceById(user.id()));
        }
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<StockTransactionResponse> search(ReportFilterRequest f, Pageable pageable) {
        return PagedResult.of(transactionRepository.search(f.warehouseId(), f.productId(), f.type(),
                f.fromOrDefault().atStartOfDay(), f.toOrDefault().plusDays(1).atStartOfDay(), pageable),
                mapper::toTransactionResponse);
    }
}
