~~Jogador inicial aleatório~~
~~Visualização das jogadas possíveis é opcional dependendo do contexto~~
~~Jogo termina quando não há peças possíveis, ou jogadas possíveis~~
~~Ganha jogador com mais peças~~

Peças especiais (1 vez por jogo):
~~Peça bomba - substitui peça do jogador, limpa area 3x3~~
~~Troca de peças - pressionar 2 peças proprias e 1 uma peça do oponente, e trocam de cor com o adversário~~

Modos (ver layouts):
~~Modo 1 - 2 jogadores, tabuleiro 8x8, 1 dispositivo~~
~~Modo 2 - 2 jogadores, tabuleiro 8x8, dispositivo diferentes~~
~~Modo 2 - 3 jogadores, tabuleiro 10x10~~

Gestão do jogo:
~~Apenas o host é que gere o jogo e efetua processamento no tabuleiro~~
~~Clientes enviam jogada ao host~~
~~Server envia atualização apos cada jogada~~
~~Fotografias dos jogadores devem ser visíveis em cada dispositivo~~

Ligação:
~~Ligação com socket direto entre os IPs~~
~~Informação tem de ser enviada em formato JSON~~
~~Caso problema de rede, todos os jogadores jogo muda para 1 diapositivo ou termina (apareçer escolha)~~

Perfil (único):
~~Fotografia - recorrer a APIs da câmara, não utilizar content providers~~
~~Modo 1 - não interessa perfil~~
~~Modo 2 e 3 - Possível ver o nome e fotografia dos jogadores~~

Scores:
~~Manter 5 top scores de cada jogador~~
~~Utilizar FireStore para armazenar as cenas~~
~~Cenas - score de cada jogador, nome e fotografia~~

Outros:
~~Permitir rodar o ecrã (fazer layouts para cada direção)~~
~~Suportar pelo menos PT, EN~~
~~Página de "About"~~ 