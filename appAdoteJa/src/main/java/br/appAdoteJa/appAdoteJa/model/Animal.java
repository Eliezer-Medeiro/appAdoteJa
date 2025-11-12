package br.appAdoteJa.appAdoteJa.model;

import br.appAdoteJa.appAdoteJa.model.Usuario;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    private String especie;

    @NotBlank
    private String raca;

    private String porte;
    
    private String sexo;

    private int idade;
    
    private String status = "Disponível";

    @Column(length = 1000)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "dono_id")
    private Usuario dono;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Foto> fotos = new ArrayList<>();

    // Método auxiliar para garantir vínculo bidirecional
    public void adicionarFoto(Foto foto) {
        foto.setAnimal(this);  // vincula o animal à foto
        this.fotos.add(foto);
    }

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public Usuario getDono() { return dono; }
    public void setDono(Usuario dono) { this.dono = dono; }

    public List<Foto> getFotos() { return fotos; }
    public void setFotos(List<Foto> fotos) { this.fotos = fotos; }
}
