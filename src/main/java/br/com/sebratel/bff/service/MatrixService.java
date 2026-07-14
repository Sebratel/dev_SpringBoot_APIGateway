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
import java.util.Arrays;
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
                return MatrixMassiveOutputDTO
                        .builder()
                        .status("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            log.info("Encontrado personId {}, iniciando procura de contrato pelo CPF", personEntity.get().getId());

            Optional<ContractProjection> contractProjection = personRepository.findContractByCPF(cpf);

            if (contractProjection.isEmpty()) {
                log.info("CLIENTE DE CPF {} NÃO TEM CONTRATO VINCULADO", cpf);
                return MatrixMassiveOutputDTO
                        .builder()
                        .status("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            ContractProjection contract = contractProjection.get();
            log.info("Encontrado contrato {}", contract.getContractId());
            Optional<AffectedUsersEntity> usuarioAfetadoEntity = affectedUserRepository
                    .findFirstByContractId(contract.getContractId());
            log.info("Realizada pesquisa de usuario afetado para contrato {} com cpf {}", contract.getContractId(), cpf);
            if (usuarioAfetadoEntity.isEmpty()) {
                log.info("NÃO FOI ENCONTRADO CLIENTE DE CONTRACT ID {}", contract.getContractId());
                return MatrixMassiveOutputDTO
                        .builder()
                        .status("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE_OFFSET);
            AffectedUsersEntity usuarioAfetado = usuarioAfetadoEntity.get();
            long minutesBetween = Duration.between(now, usuarioAfetado.getFinishDate()).toMinutes();
            Integer numberOfHours = (minutesBetween <= 0) ? 1 : (int) Math.ceil(minutesBetween / 60.0);

            return MatrixMassiveOutputDTO
                    .builder()
                    .resolutionTime(numberOfHours)
                    .resolutionTimeHour("" + usuarioAfetado.getFinishDate().getHour())
                    .authenticationProblems(1L)
                    .status("client_found")
                    .build();
        }catch (Exception e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            return MatrixMassiveOutputDTO
                    .builder()
                    .status("not_found_client")
                    .authenticationProblems(0L)
                    .resolutionTimeHour("23")
                    .authenticationProblems(0L)
                    .build();
        }
    }


}
