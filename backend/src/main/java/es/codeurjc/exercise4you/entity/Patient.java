package es.codeurjc.exercise4you.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NamedNativeQuery(
    name = "Patient.findPatientDtoByUsrId",
    query = "SELECT p.name, p.surnames, p.gender, p.birthdate FROM patients p WHERE p.usr_id = :usrId",
    resultSetMapping = "Mapping.PatientDTO")
@SqlResultSetMapping(
    name = "Mapping.PatientDTO",
    classes = @ConstructorResult(targetClass = PatientDTO.class, columns = {
        @ColumnResult(name = "name"),
        @ColumnResult(name = "surnames"),
        @ColumnResult(name = "gender"),
        @ColumnResult(name = "birthdate")})
)

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;
    @JsonBackReference
    @ManyToOne
    private Usr usr;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surnames;
    @Column(nullable = false)
    private String gender;
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date birthdate;
}
