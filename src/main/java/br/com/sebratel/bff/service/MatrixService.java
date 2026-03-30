package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

        UsuarioAfetadoEntity usuarioAfetadoEntity = usuarioAfetadoRepository
                .findByContractId(contractProjection.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Não existe contract_id vinculado ao CPF"));

        LocalDateTime now = LocalDateTime.now();
        long hoursBetween = ChronoUnit.HOURS.between(now, usuarioAfetadoEntity.getFinishDate());
        Integer numberOfHours = (hoursBetween <= 0) ? 1 : (int) hoursBetween;

        return MatrixMassiveOutputDTO
                .builder()
                .resolutionTime(numberOfHours)
                .resolutionTimeHour("" + usuarioAfetadoEntity.getFinishDate().getHour())
                .authenticationProblems(1L)
                .build();
    }


}
