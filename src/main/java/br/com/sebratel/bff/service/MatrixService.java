package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class MatrixService {

    final PersonRepository personRepository;
    final UsuarioAfetadoRepository usuarioAfetadoRepository;

    public MatrixService(PersonRepository personRepository, UsuarioAfetadoRepository usuarioAfetadoRepository) {
        this.personRepository = personRepository;
        this.usuarioAfetadoRepository = usuarioAfetadoRepository;
    }

    public MatrixMassiveOutputDTO getContractInfoByCPF(String cpf) {

        try {
            log.info("Iniciando busca de contrato para o CPF: {}", cpf);
            Optional<PersonEntity> personEntity = personRepository.findByTxId(cpf);

            if (personEntity.isEmpty()) {
                return MatrixMassiveOutputDTO
                        .builder()
                        .statusCliente("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            System.out.println(personEntity.get().getId());

            Optional<ContractProjection> contractProjection = personRepository.findContractByCPF(cpf);

            if (contractProjection.isEmpty()) {
                log.error("CLIENTE DE CPF {} NÃO TEM CONTRATO VINCULADO", cpf);
                return MatrixMassiveOutputDTO
                        .builder()
                        .statusCliente("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            ContractProjection contract = contractProjection.get();
            Optional<UsuarioAfetadoEntity> usuarioAfetadoEntity = usuarioAfetadoRepository
                    .findByContractId(contract.getContractId());

            if (usuarioAfetadoEntity.isEmpty()) {
                log.error("NÃO FOI ENCONTRADO CLIENTE DE CONTRACT ID {}", contract.getContractId());
                return MatrixMassiveOutputDTO
                        .builder()
                        .statusCliente("not_found_client")
                        .authenticationProblems(0L)
                        .resolutionTimeHour("23")
                        .authenticationProblems(0L)
                        .build();
            }

            LocalDateTime now = LocalDateTime.now();
            UsuarioAfetadoEntity usuarioAfetado = usuarioAfetadoEntity.get();
            long hoursBetween = ChronoUnit.HOURS.between(now, usuarioAfetado.getFinishDate());
            Integer numberOfHours = (hoursBetween <= 0) ? 1 : (int) hoursBetween;

            return MatrixMassiveOutputDTO
                    .builder()
                    .resolutionTime(numberOfHours)
                    .resolutionTimeHour("" + usuarioAfetado.getFinishDate().getHour())
                    .authenticationProblems(1L)
                    .statusCliente("client_found")
                    .build();
        }catch (Exception e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            return MatrixMassiveOutputDTO
                    .builder()
                    .statusCliente("not_found_client")
                    .authenticationProblems(0L)
                    .resolutionTimeHour("23")
                    .authenticationProblems(0L)
                    .build();
        }
    }


}
