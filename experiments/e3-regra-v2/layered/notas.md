# E3-v2 — Layered — notas

Branch `exp2/e3-regra-v2-layered` (P `0f46387`, T `ce636e7`, commit vazio). Execução em 2026-10-07 (quarta-feira).

## Produção (commit P)

| Arquivo | + | − | Pacote |
|---------|---|---|--------|
| `service/ReservationService.java` | 2 | 2 | `service` |

## Testes quebrados após P

**0 de 1.** A variante não tem teste de unidade da regra. O único teste é o de aceitação, que passou porque o slot ficou 7 dias à frente.

## Ajuste de testes (commit T)

Nenhum. O commit T é vazio, só para manter o protocolo uniforme.

## ArchUnit

13/13 regras verdes. Métricas idênticas à `baseline-v2`.

## Leitura

- A mudança de regra passou **silenciosa**: nenhum teste detectou a alteração de comportamento.
- Isso não decorre do estilo em camadas em si. Decorre de a regra estar num método privado de um `@Service` que também chama dois clientes HTTP e o repositório. Testá-la isoladamente exigiria três dublês (mocks), e a implementação idiomática de referência não os tinha.
- **Fragilidade latente (H3.4):** a suíte de aceitação escolhe "a próxima quarta-feira com pelo menos 2 dias de distância", às 10:00. Executada numa **segunda-feira depois das 10:00**, essa quarta fica a menos de 48 h, e o teste de aceitação quebra **nas duas variantes**, por motivo de calendário e não de arquitetura.
