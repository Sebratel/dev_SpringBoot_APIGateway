package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("afetadosEmployeeRepository")
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
}
