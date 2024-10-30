package es.codeurjc.exercise4you.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToMany;
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
    query = "SELECT p.id, p.name, p.surnames, p.gender, p.birthdate FROM patients p WHERE p.usr_id = :usrId",
    resultSetMapping = "Mapping.PatientDTO")
@SqlResultSetMapping(
    name = "Mapping.sdf",
    classes = @ConstructorResult(targetClass = PatientDTO.class, columns = {
        @ColumnResult(name = "id"),
        @ColumnResult(name = "name"),
        @ColumnResult(name = "surnames"),
        @ColumnResult(name = "gender"),
        @ColumnResult(name = "birthdate")})
)

@NamedNativeQuery(
    name = "Patient.customFindPatientDtoByUsrIdAndNameContainingAndSurnamesContaining",
    query = "SELECT p.id, p.name, p.surnames, p.gender, p.birthdate FROM patients p WHERE p.usr_id = :usrId AND concat(p.name,' ',p.surnames) like CONCAT('%','ilario','%')",
    resultSetMapping = "Mapping.PatientDTO")
@SqlResultSetMapping(
    name = "Mapping.PatientDTO",
    classes = @ConstructorResult(targetClass = PatientDTO.class, columns = {
        @ColumnResult(name = "id"),
        @ColumnResult(name = "name"),
        @ColumnResult(name = "surnames"),
        @ColumnResult(name = "gender"),
        @ColumnResult(name = "birthdate")}
    )
)

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;
    @JsonBackReference
    @ManyToOne
    private Usr usr;
    @Column(nullable = false, length = 32)
    private String name;
    @Column(nullable = false, length = 32)
    private String surnames;
    @Column(nullable = false)
    private String gender;
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate birthdate;
    @JsonBackReference
    @OneToMany(mappedBy = "patientId")
    private Collection<DataRecord> dataRecord;
}
