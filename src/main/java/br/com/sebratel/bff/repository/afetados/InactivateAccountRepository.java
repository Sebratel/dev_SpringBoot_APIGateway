package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.InactivateAccountEntity;

import java.util.List;
import java.util.Optional;

public interface InactivateAccountRepository {
    InactivateAccountEntity save(InactivateAccountEntity entity);
}
