package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class MatrixService {

    final PersonRepository personRepository;
    final UsuarioAfetadoRepository usuarioAfetadoRepository;

    public MatrixService(PersonRepository personRepository, UsuarioAfetadoRepository usuarioAfetadoRepository) {
        this.personRepository = personRepository;
        this.usuarioAfetadoRepository = usuarioAfetadoRepository;
    }

    public MatrixMassiveOutputDTO getContractInfoByCPF(String cpf) {
        PersonEntity personEntity = personRepository.findByTxId(cpf);
        System.out.println(personEntity.getId());

        ContractProjection contractProjection = personRepository.findContractByCPF(cpf);

        Optional<UsuarioAfetadoEntity> usuarioAfetadoEntity = usuarioAfetadoRepository
                .findByContractId(contractProjection.getContractId());

        if(usuarioAfetadoEntity.isEmpty()) {
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
    }


}
