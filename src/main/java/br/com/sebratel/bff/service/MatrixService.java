package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.model.entity.AffectedUsersEntity;
import br.com.sebratel.bff.repository.afetados.AffectedUserRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MatrixService {

    private static final ZoneOffset APPLICATION_ZONE_OFFSET = ZoneOffset.of("-03:00");

    final PersonRepository personRepository;
    final AffectedUserRepository affectedUserRepository;

    public MatrixService(PersonRepository personRepository, AffectedUserRepository affectedUserRepository) {
        this.personRepository = personRepository;
        this.affectedUserRepository = affectedUserRepository;
    }

    public MatrixMassiveOutputDTO getContractInfoByCPF(String cpf) {

        try {
            log.info("Iniciando busca de contrato para o CPF: {}", cpf);
            Optional<PersonEntity> personEntity = personRepository.findByTxId(cpf);

            if (personEntity.isEmpty()) {
                log.warn("Nenhum person encontrado: {}", cpf);
                return notFoundClient();
            }

            log.info("Encontrado personId {}, iniciando procura de contrato pelo CPF", personEntity.get().getId());

            List<ContractProjection> contracts = personRepository.findContractsByCPF(cpf);

            if (contracts.isEmpty()) {
                log.info("Cliente de CPF {} nao tem contrato vinculado", cpf);
                return notFoundClient();
            }

            log.info("Encontrados {} contratos para o CPF, verificando massiva ativa em cada um", contracts.size());

            for (ContractProjection contract : contracts) {
                Optional<AffectedUsersEntity> usuarioAfetadoEntity = affectedUserRepository
                        .findFirstByContractId(contract.getContractId());

                if (usuarioAfetadoEntity.isEmpty()) {
                    log.info("Nenhum usuario afetado para o contrato {}", contract.getContractId());
                    continue;
                }

                AffectedUsersEntity usuarioAfetado = usuarioAfetadoEntity.get();
                log.info("Massiva ativa encontrada para o contrato {}", contract.getContractId());

                LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE_OFFSET);
                long minutesBetween = Duration.between(now, usuarioAfetado.getFinishDate()).toMinutes();
                Integer numberOfHours = (minutesBetween <= 0) ? 1 : (int) Math.ceil(minutesBetween / 60.0);

                return MatrixMassiveOutputDTO
                        .builder()
                        .resolutionTime(numberOfHours)
                        .resolutionTimeHour("" + usuarioAfetado.getFinishDate().getHour())
                        .authenticationProblems(1L)
                        .status("client_found")
                        .build();
            }

            log.info("Nenhum dos {} contratos do CPF possui massiva ativa", contracts.size());
            return notFoundClient();
        }catch (Exception e) {
            log.info("Não foi possível localizar contrato para o CPF {}: {}", cpf, e.getMessage());
            return notFoundClient();
        }
    }

    private MatrixMassiveOutputDTO notFoundClient() {
        return MatrixMassiveOutputDTO
                .builder()
                .status("not_found_client")
                .authenticationProblems(0L)
                .resolutionTimeHour("23")
                .build();
    }


}
