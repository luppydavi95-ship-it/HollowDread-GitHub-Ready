# Hollow Dread — pacote preparado para compilação na nuvem

Este pacote junta o código do Hollow Dread ao MDK oficial do Forge 1.20.1-47.4.28 e inclui um workflow do GitHub Actions. O wrapper está configurado para Gradle 8.8 (não Gradle 9.7.1).

## Como gerar o JAR sem compilar no seu PC

1. Entre em https://github.com/ e crie uma conta, se ainda não tiver.
2. Crie um repositório novo, por exemplo `HollowDread`. Para facilitar, deixe-o **Public**; se preferir **Private**, confirme que sua conta tem minutos do Actions disponíveis.
3. Na página do repositório, clique em **Add file → Upload files**.
4. Extraia este ZIP no seu computador. Envie **o conteúdo de dentro da pasta** (incluindo `gradlew`, `gradlew.bat`, `gradle`, `src`, `build.gradle`, `gradle.properties`, `settings.gradle` e `.github`) para a página de upload do GitHub. Não envie apenas o ZIP dentro do repositório.
5. Clique em **Commit changes**.
6. Abra a aba **Actions** do repositório. O workflow **Build Hollow Dread** deve iniciar após o commit. Se não iniciar, abra o workflow e clique em **Run workflow**.
7. Abra a execução mais recente. Aguarde até ficar verde (**Success**). Se ficar vermelho (**Failure**), abra a etapa **Build mod** e copie o erro para me enviar.
8. Na execução concluída, encontre **Artifacts** e baixe `hollowdread-1.0.0`. Extraia o ZIP baixado; dentro estará o `.jar`.
9. Coloque o `.jar` em `%appdata%\.minecraft\mods` e inicie Minecraft Java 1.20.1 com Forge 47.4.28.

## Importante

- O workflow compila o projeto; ele não corrige automaticamente erros de código.
- A compilação ainda precisa ser executada no GitHub para confirmar que todos os fontes compilam. Não foi afirmado que o JAR já foi gerado.
- Use Java 17 e Minecraft Java 1.20.1 com Forge 47.4.28.
- Não compartilhe tokens/senhas do GitHub com ninguém.

## Arquivos do projeto

O código-fonte original e os recursos do mod estão em `src/`. A configuração do Forge/Gradle e o wrapper são os do MDK 1.20.1-47.4.28.


---

# Hollow Dread — mod de terror psicológico (Forge 1.20.1-47.4.28)

## Como compilar

O `forge-...-installer.jar` serve só para **instalar** o Forge num cliente/servidor.
Para **compilar um mod** você precisa do ambiente de desenvolvimento (MDK):

1. Baixe o **MDK do Forge 1.20.1-47.4.28** em https://files.minecraftforge.net/ (botão "Mdk").
2. Extraia o MDK numa pasta e copie para ela, sobrescrevendo:
   `build.gradle`, `gradle.properties`, `settings.gradle` e a pasta `src/` deste projeto
   (se preferir, mantenha o `gradlew` + `gradle/wrapper/` do MDK e use só o `src/`).
3. Precisa de **JDK 17**. Rode:
   ```
   ./gradlew build          # gera build/libs/hollowdread-1.0.0.jar
   ./gradlew runClient      # abre o Minecraft de teste com o mod
   ```
4. Coloque o `.jar` em `mods/` de um Minecraft 1.20.1 com Forge 47.4.28.

## O que o mod faz

### Sistema de Sanidade (0–100)
Capability persistente por jogador, sincronizada com o cliente. Cai com: escuridão, cavernas, noite/tempestade,
Nether/End, entidades do terror por perto, bonecas no inventário e o efeito *Pavor*.
Sobe com: luz, dormir, Lanterna da Razão, Velas de Proteção e Pílulas Lúcidas.

### Diretor do Medo (`DreadDirector`)
Uma IA de ritmo que roda por jogador. Em vez de sustos aleatórios, ele usa um cooldown dinâmico (menos sanidade = sustos
mais frequentes) e sorteia eventos ponderados que só desbloqueiam abaixo de certos limites:

| Sanidade | Eventos |
|---|---|
| < 85 | ruídos de caverna, passos atrás de você, sussurros na tela |
| < 70 | respiração no seu ouvido, batidas na parede |
| < 60 | coração acelerado, chiado de Creeper falso, porta rangendo, **tochas próximas se apagam** |
| < 50 | flash preto, **Observador** aparece ao longe |
| < 45 | **alucinações** (entidade que só você vê, enviada só ao seu cliente) |
| < 40 | **portas reais batem sozinhas** |
| < 38 | emboscada de Rastejantes no escuro |
| < 32 | pulso de escuridão |
| < 30 | jumpscare |
| < 25 | **o Perseguidor** é invocado atrás de você |

Na tela: vinheta pulsante, câmera que balança e inclina, neblina que fecha, batimentos cardíacos.

### Entidades
- **O Perseguidor** — alto, sem rosto. *Congela totalmente enquanto alguém o observa* (linha de visão + ângulo), corre
  quando ninguém olha, teleporta para as suas costas e some depois de atacar. Velas de Proteção o afastam.
- **O Observador** — silhueta parada ao longe, sempre virada para você. Some se você o encara por 4 s ou se aproxima;
  **rasteja para mais perto toda vez que você desvia o olhar**.
- **Rastejante Oco** — escala paredes, só vive no escuro, foge de luz forte e da Lanterna da Razão, ataca em bate-e-corre.
  Único que spawna naturalmente (cavernas/escuro, via biome modifier).

### Itens e blocos
Pílula Lúcida (overdose com 3 doses seguidas), Tomo do Vazio (lore, custa sanidade, pode atrair um Observador),
Boneca Amaldiçoada, Lanterna da Razão (durabilidade, reparo com glowstone), Vela de Proteção, Lâmpada Tremeluzente
(pisca e apaga conforme a sanidade de quem está perto), efeito *Pavor*, ovos de geração, aba criativa, receitas e loot.

### Comandos (OP nível 2)
```
/dread sanity get <jogador>
/dread sanity set <jogador> <0-100>
/dread trigger <jogador> <evento>     (tab completa: stalker, ghost, jumpscare, door_slam, ...)
```
Dica de teste: jogadores em criativo/espectador são ignorados pelo Diretor; use survival e
`/dread sanity set @s 20`, ou dispare eventos direto com `/dread trigger`.

### Configuração
`config/hollowdread-common.toml` (multiplicadores, tempos entre sustos, ligar/desligar Perseguidor, Observador, tochas,
portas, jumpscares) e `config/hollowdread-client.toml` (HUD, vinheta, câmera, neblina, **jumpscares** — desligue se tiver
fotossensibilidade).

## Estrutura
```
com.hollowdread
├─ sanity/    capability + sincronização
├─ scare/     DreadDirector, DreadEvent, DreadSpawner, Ghosts (observação/alucinação)
├─ entity/    StalkerEntity, WatcherEntity, CrawlerEntity
├─ network/   SimpleChannel: SyncSanityPacket, ScarePacket
├─ client/    HUD, vinheta, câmera, neblina, ClientScareManager, renderers
├─ block/ item/ effect/ init/ event/ command/ config/
```
Os sons são todos sons vanilla (Warden, Enderman, portas...), recombinados em pitch/volume/sequência. As texturas foram
geradas por script e são placeholders funcionais — troque os PNGs em `assets/hollowdread/textures` quando quiser.
