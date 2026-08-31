export interface StateLocation {
  name: string;
  code?: string;
  cities: string[];
}

export interface CountryLocation {
  name: string;
  code: string;
  states: StateLocation[];
}

export interface LocationOption {
  value: string;
  label: string;
}

export const LOCATION_DATA: CountryLocation[] = [
  {
    "name": "Brasil",
    "code": "BR",
    "states": [
      {
        "name": "Acre",
        "code": "AC",
        "cities": [
          "Rio Branco",
          "Cruzeiro do Sul",
          "Sena Madureira",
          "Tarauacá",
          "Feijó",
          "Brasiléia",
          "Senador Guiomard",
          "Plácido de Castro",
          "Xapuri"
        ]
      },
      {
        "name": "Alagoas",
        "code": "AL",
        "cities": [
          "Maceió",
          "Arapiraca",
          "Rio Largo",
          "Palmeira dos Índios",
          "União dos Palmares",
          "Penedo",
          "São Miguel dos Campos",
          "Delmiro Gouveia",
          "Coruripe",
          "Marechal Deodoro",
          "Santana do Ipanema"
        ]
      },
      {
        "name": "Amapá",
        "code": "AP",
        "cities": [
          "Macapá",
          "Santana",
          "Laranjal do Jari",
          "Oiapoque",
          "Porto Grande",
          "Mazagão",
          "Tartarugalzinho",
          "Vitória do Jari"
        ]
      },
      {
        "name": "Amazonas",
        "code": "AM",
        "cities": [
          "Manaus",
          "Parintins",
          "Itacoatiara",
          "Manacapuru",
          "Coari",
          "Tabatinga",
          "Maués",
          "Tefé",
          "Manicoré",
          "Humaitá",
          "Iranduba",
          "São Gabriel da Cachoeira",
          "Benjamin Constant"
        ]
      },
      {
        "name": "Bahia",
        "code": "BA",
        "cities": [
          "Salvador",
          "Feira de Santana",
          "Vitória da Conquista",
          "Camaçari",
          "Juazeiro",
          "Itabuna",
          "Lauro de Freitas",
          "Ilhéus",
          "Jequié",
          "Teixeira de Freitas",
          "Alagoinhas",
          "Barreiras",
          "Porto Seguro",
          "Simões Filho",
          "Paulo Afonso",
          "Eunápolis",
          "Santo Antônio de Jesus",
          "Valença",
          "Candeias",
          "Guanambi",
          "Jacobina",
          "Serrinha",
          "Senhor do Bonfim",
          "Dias d'Ávila",
          "Luís Eduardo Magalhães",
          "Itapetinga",
          "Irecê",
          "Campo Formoso",
          "Casa Nova",
          "Brumado"
        ]
      },
      {
        "name": "Ceará",
        "code": "CE",
        "cities": [
          "Fortaleza",
          "Caucaia",
          "Juazeiro do Norte",
          "Maracanaú",
          "Sobral",
          "Crato",
          "Itapipoca",
          "Maranguape",
          "Iguatu",
          "Quixadá",
          "Pacatuba",
          "Aquiraz",
          "Quixeramobim",
          "Canindé",
          "Tianguá",
          "Russas",
          "Crateús",
          "Aracati",
          "Cascavel",
          "Horizonte",
          "Camocim",
          "Acaraú",
          "Morada Nova",
          "Barbalha",
          "Limoeiro do Norte"
        ]
      },
      {
        "name": "Distrito Federal",
        "code": "DF",
        "cities": [
          "Brasília",
          "Plano Piloto",
          "Taguatinga",
          "Ceilândia",
          "Águas Claras",
          "Samambaia",
          "Gama",
          "Guará",
          "Sobradinho",
          "Santa Maria",
          "Vicente Pires",
          "Sudoeste/Octogonal",
          "Recanto das Emas",
          "Planaltina",
          "Riacho Fundo",
          "Lago Sul",
          "Lago Norte",
          "Cruzeiro",
          "Núcleo Bandeirante",
          "Brazlândia",
          "São Sebastião",
          "Paranoá",
          "Itapoã",
          "Jardim Botânico",
          "SCIA/Estrutural"
        ]
      },
      {
        "name": "Espírito Santo",
        "code": "ES",
        "cities": [
          "Vitória",
          "Vila Velha",
          "Serra",
          "Cariacica",
          "Cachoeiro de Itapemirim",
          "Linhares",
          "São Mateus",
          "Colatina",
          "Guarapari",
          "Aracruz",
          "Viana",
          "Nova Venécia",
          "Barra de São Francisco",
          "Marataízes",
          "Castelo",
          "Santa Maria de Jetibá",
          "Domingos Martins",
          "Anchieta",
          "Itapemirim"
        ]
      },
      {
        "name": "Goiás",
        "code": "GO",
        "cities": [
          "Goiânia",
          "Aparecida de Goiânia",
          "Anápolis",
          "Rio Verde",
          "Luziânia",
          "Águas Lindas de Goiás",
          "Valparaíso de Goiás",
          "Trindade",
          "Formosa",
          "Novo Gama",
          "Senador Canedo",
          "Itumbiara",
          "Catalão",
          "Jataí",
          "Planaltina",
          "Caldas Novas",
          "Santo Antônio do Descoberto",
          "Goianésia",
          "Cidade Ocidental",
          "Mineiros",
          "Cristalina",
          "Inhumas",
          "Quirinópolis",
          "Jaraguá",
          "Niquelândia",
          "Porangatu"
        ]
      },
      {
        "name": "Maranhão",
        "code": "MA",
        "cities": [
          "São Luís",
          "Imperatriz",
          "São José de Ribamar",
          "Timon",
          "Caxias",
          "Codó",
          "Paço do Lumiar",
          "Açailândia",
          "Bacabal",
          "Balsas",
          "Santa Inês",
          "Barra do Corda",
          "Pinheiro",
          "Chapadinha",
          "Santa Luzia",
          "Buriticupu",
          "Grajaú",
          "Itapecuru Mirim",
          "Coroatá",
          "Tutóia",
          "Viana",
          "Presidente Dutra"
        ]
      },
      {
        "name": "Mato Grosso",
        "code": "MT",
        "cities": [
          "Cuiabá",
          "Várzea Grande",
          "Rondonópolis",
          "Sinop",
          "Tangará da Serra",
          "Sorriso",
          "Lucas do Rio Verde",
          "Primavera do Leste",
          "Barra do Garças",
          "Cáceres",
          "Alta Floresta",
          "Nova Mutum",
          "Pontes e Lacerda",
          "Juína",
          "Campo Verde",
          "Campo Novo do Parecis",
          "Peixoto de Azevedo",
          "Barra do Bugres",
          "Guarantã do Norte",
          "Poconé"
        ]
      },
      {
        "name": "Mato Grosso do Sul",
        "code": "MS",
        "cities": [
          "Campo Grande",
          "Dourados",
          "Três Lagoas",
          "Corumbá",
          "Ponta Porã",
          "Sidrolândia",
          "Naviraí",
          "Nova Andradina",
          "Aquidauana",
          "Maracaju",
          "Paranaíba",
          "Amambai",
          "Rio Brilhante",
          "Coxim",
          "Caarapó",
          "Bonito",
          "Miranda",
          "São Gabriel do Oeste",
          "Aparecida do Taboado"
        ]
      },
      {
        "name": "Minas Gerais",
        "code": "MG",
        "cities": [
          "Belo Horizonte",
          "Uberlândia",
          "Contagem",
          "Juiz de Fora",
          "Betim",
          "Montes Claros",
          "Ribeirão das Neves",
          "Uberaba",
          "Governador Valadares",
          "Ipatinga",
          "Sete Lagoas",
          "Divinópolis",
          "Santa Luzia",
          "Ibirité",
          "Poços de Caldas",
          "Patos de Minas",
          "Pouso Alegre",
          "Teófilo Otoni",
          "Barbacena",
          "Sabará",
          "Varginha",
          "Conselheiro Lafaiete",
          "Araguari",
          "Itabira",
          "Passos",
          "Coronel Fabriciano",
          "Muriaé",
          "Ubá",
          "Nova Lima",
          "Ituiutaba",
          "Lavras",
          "Itajubá",
          "Pará de Minas",
          "Paracatu",
          "Itaúna",
          "Caratinga",
          "Nova Serrana",
          "São João del-Rei",
          "Manhuaçu",
          "Viçosa",
          "Timóteo",
          "Alfenas",
          "Unaí",
          "Curvelo",
          "Ouro Preto",
          "Três Corações",
          "Mariana",
          "Diamantina",
          "Cataguases",
          "Araxá",
          "Frutal",
          "Formiga",
          "Ponte Nova",
          "Januária"
        ]
      },
      {
        "name": "Pará",
        "code": "PA",
        "cities": [
          "Belém",
          "Ananindeua",
          "Santarém",
          "Marabá",
          "Parauapebas",
          "Castanhal",
          "Abaetetuba",
          "Cametá",
          "Marituba",
          "Bragança",
          "São Félix do Xingu",
          "Barcarena",
          "Altamira",
          "Tucuruí",
          "Paragominas",
          "Tailândia",
          "Breves",
          "Itaituba",
          "Redenção",
          "Moju",
          "Novo Repartimento",
          "Oriximiná",
          "Capanema",
          "Santa Izabel do Pará"
        ]
      },
      {
        "name": "Paraíba",
        "code": "PB",
        "cities": [
          "João Pessoa",
          "Campina Grande",
          "Santa Rita",
          "Patos",
          "Bayeux",
          "Sousa",
          "Cajazeiras",
          "Cabedelo",
          "Guarabira",
          "Mamanguape",
          "Queimadas",
          "Pombal",
          "Monteiro",
          "Esperança",
          "Catolé do Rocha",
          "Alagoa Grande",
          "Solânea",
          "Princesa Isabel"
        ]
      },
      {
        "name": "Paraná",
        "code": "PR",
        "cities": [
          "Curitiba",
          "Londrina",
          "Maringá",
          "Ponta Grossa",
          "Cascavel",
          "São José dos Pinhais",
          "Foz do Iguaçu",
          "Colombo",
          "Guarapuava",
          "Paranaguá",
          "Araucária",
          "Toledo",
          "Apucarana",
          "Pinhais",
          "Campo Largo",
          "Arapongas",
          "Almirante Tamandaré",
          "Piraquara",
          "Umuarama",
          "Cambé",
          "Fazenda Rio Grande",
          "Sarandi",
          "Campo Mourão",
          "Francisco Beltrão",
          "Paranavaí",
          "Pato Branco",
          "Cianorte",
          "Telêmaco Borba",
          "Castro",
          "Rolândia",
          "Irati",
          "União da Vitória",
          "Ibiporã",
          "Marechal Cândido Rondon",
          "Medianeira",
          "Cornélio Procópio",
          "Palmas",
          "Lapa",
          "Santo Antônio da Platina",
          "Dois Vizinhos",
          "Guaratuba",
          "Matinhos",
          "Porecatu"
        ]
      },
      {
        "name": "Pernambuco",
        "code": "PE",
        "cities": [
          "Recife",
          "Jaboatão dos Guararapes",
          "Olinda",
          "Caruaru",
          "Petrolina",
          "Paulista",
          "Cabo de Santo Agostinho",
          "Camaragibe",
          "Garanhuns",
          "Vitória de Santo Antão",
          "Igarassu",
          "São Lourenço da Mata",
          "Santa Cruz do Capibaribe",
          "Abreu e Lima",
          "Ipojuca",
          "Serra Talhada",
          "Araripina",
          "Gravatá",
          "Carpina",
          "Goiana",
          "Belo Jardim",
          "Arcoverde",
          "Ouricuri",
          "Pesqueira",
          "Surubim",
          "Palmares",
          "Bezerros",
          "Salgueiro",
          "Escada"
        ]
      },
      {
        "name": "Piauí",
        "code": "PI",
        "cities": [
          "Teresina",
          "Parnaíba",
          "Picos",
          "Piripiri",
          "Floriano",
          "Barras",
          "Campo Maior",
          "União",
          "Altos",
          "Esperantina",
          "José de Freitas",
          "Pedro II",
          "Oeiras",
          "São Raimundo Nonato",
          "Miguel Alves",
          "Luís Correia",
          "Bom Jesus",
          "Corrente"
        ]
      },
      {
        "name": "Rio de Janeiro",
        "code": "RJ",
        "cities": [
          "Rio de Janeiro",
          "São Gonçalo",
          "Duque de Caxias",
          "Nova Iguaçu",
          "Niterói",
          "Belford Roxo",
          "Campos dos Goytacazes",
          "São João de Meriti",
          "Petrópolis",
          "Volta Redonda",
          "Magé",
          "Itaboraí",
          "Mesquita",
          "Macaé",
          "Cabo Frio",
          "Nova Friburgo",
          "Barra Mansa",
          "Angra dos Reis",
          "Teresópolis",
          "Nilópolis",
          "Queimados",
          "Maricá",
          "Resende",
          "Rio das Ostras",
          "Araruama",
          "Itaperuna",
          "Itaguaí",
          "Japeri",
          "São Pedro da Aldeia",
          "Barra do Piraí",
          "Seropédica",
          "Saquarema",
          "Três Rios",
          "Valença",
          "Guapimirim",
          "Rio Bonito",
          "Cachoeiras de Macacu",
          "Paracambi",
          "Armação dos Búzios",
          "Arraial do Cabo",
          "Paraty",
          "Mangaratiba",
          "Casimiro de Abreu"
        ]
      },
      {
        "name": "Rio Grande do Norte",
        "code": "RN",
        "cities": [
          "Natal",
          "Mossoró",
          "Parnamirim",
          "São Gonçalo do Amarante",
          "Ceará-Mirim",
          "Macaíba",
          "Caicó",
          "Assu",
          "São José de Mipibu",
          "Currais Novos",
          "Santa Cruz",
          "Nova Cruz",
          "Apodi",
          "João Câmara",
          "Canguaretama",
          "Touros",
          "Macau",
          "Pau dos Ferros",
          "Areia Branca",
          "Extremoz",
          "Tibau do Sul (Pipa)"
        ]
      },
      {
        "name": "Rio Grande do Sul",
        "code": "RS",
        "cities": [
          "Porto Alegre",
          "Caxias do Sul",
          "Canoas",
          "Pelotas",
          "Santa Maria",
          "Gravataí",
          "Viamão",
          "Novo Hamburgo",
          "São Leopoldo",
          "Rio Grande",
          "Alvorada",
          "Passo Fundo",
          "Sapucaia do Sul",
          "Santa Cruz do Sul",
          "Cachoeirinha",
          "Uruguaiana",
          "Bento Gonçalves",
          "Bagé",
          "Erechim",
          "Guaíba",
          "Santa Rosa",
          "Santana do Livramento",
          "Lajeado",
          "Ijuí",
          "Esteio",
          "Sapiranga",
          "Farroupilha",
          "Venâncio Aires",
          "Vacaria",
          "Campo Bom",
          "Montenegro",
          "Cruz Alta",
          "Carazinho",
          "São Gabriel",
          "São Borja",
          "Taquara",
          "Canguçu",
          "Parobé",
          "Gramado",
          "Canela",
          "Capão da Canoa",
          "Tramandaí",
          "Torres",
          "Santiago",
          "Panambi",
          "Marau",
          "Estância Velha",
          "Osório"
        ]
      },
      {
        "name": "Rondônia",
        "code": "RO",
        "cities": [
          "Porto Velho",
          "Ji-Paraná",
          "Ariquemes",
          "Vilhena",
          "Cacoal",
          "Rolim de Moura",
          "Jaru",
          "Guajará-Mirim",
          "Ouro Preto do Oeste",
          "Pimenta Bueno",
          "Buritis",
          "Machadinho d'Oeste",
          "Espigão d'Oeste"
        ]
      },
      {
        "name": "Roraima",
        "code": "RR",
        "cities": [
          "Boa Vista",
          "Rorainópolis",
          "Caracaraí",
          "Pacaraima",
          "Cantá",
          "Mucajaí",
          "Bonfim",
          "Alto Alegre",
          "Amajari"
        ]
      },
      {
        "name": "Santa Catarina",
        "code": "SC",
        "cities": [
          "Florianópolis",
          "Joinville",
          "Blumenau",
          "São José",
          "Chapecó",
          "Itajaí",
          "Criciúma",
          "Jaraguá do Sul",
          "Palhoça",
          "Lages",
          "Balneário Camboriú",
          "Brusque",
          "Tubarão",
          "São Bento do Sul",
          "Camboriú",
          "Navegantes",
          "Caçador",
          "Concórdia",
          "Rio do Sul",
          "Gaspar",
          "Biguaçu",
          "Indaial",
          "Araranguá",
          "Mafra",
          "Canoinhas",
          "Içara",
          "Videira",
          "Itapema",
          "São Francisco do Sul",
          "Timbó",
          "São Miguel do Oeste",
          "Curitibanos",
          "Laguna",
          "Imbituba",
          "Fraiburgo",
          "Tijucas",
          "Porto União",
          "Joaçaba",
          "Pomerode",
          "Garuva",
          "Penha",
          "Bombinhas"
        ]
      },
      {
        "name": "São Paulo",
        "code": "SP",
        "cities": [
          "São Paulo",
          "Guarulhos",
          "Campinas",
          "São Bernardo do Campo",
          "São José dos Campos",
          "Santo André",
          "Ribeirão Preto",
          "Osasco",
          "Sorocaba",
          "Mauá",
          "São José do Rio Preto",
          "Mogi das Cruzes",
          "Santos",
          "Diadema",
          "Jundiaí",
          "Piracicaba",
          "Carapicuíba",
          "Bauru",
          "Itaquaquecetuba",
          "São Vicente",
          "Franca",
          "Praia Grande",
          "Guarujá",
          "Taubaté",
          "Limeira",
          "Suzano",
          "Taboão da Serra",
          "Sumaré",
          "Barueri",
          "Embu das Artes",
          "Indaiatuba",
          "Cotia",
          "São Carlos",
          "Americana",
          "Itapevi",
          "Marília",
          "Araraquara",
          "Jacareí",
          "Hortolândia",
          "Presidente Prudente",
          "Rio Claro",
          "Araçatuba",
          "Santa Bárbara d'Oeste",
          "Ferraz de Vasconcelos",
          "Francisco Morato",
          "Itapecerica da Serra",
          "Itu",
          "Bragança Paulista",
          "Pindamonhangaba",
          "Itapetininga",
          "São Caetano do Sul",
          "Franco da Rocha",
          "Mogi Guaçu",
          "Jaú",
          "Botucatu",
          "Atibaia",
          "Santana de Parnaíba",
          "Araras",
          "Valinhos",
          "Sertãozinho",
          "Votorantim",
          "Barretos",
          "Catanduva",
          "Guaratinguetá",
          "Jandira",
          "Birigui",
          "Várzea Paulista",
          "Tatuí",
          "Caraguatatuba",
          "Salto",
          "Poá",
          "Itatiba",
          "Ourinhos",
          "Paulínia",
          "Assis",
          "Leme",
          "Caieiras",
          "Mairiporã",
          "Votuporanga",
          "Ubatuba",
          "São Sebastião",
          "Avaré",
          "Lorena",
          "Bebedouro",
          "Mogi Mirim",
          "Pirassununga",
          "Amparo",
          "Fernandópolis",
          "Ilhabela",
          "Campos do Jordão",
          "Vinhedo",
          "Boituva",
          "Jaguariúna"
        ]
      },
      {
        "name": "Sergipe",
        "code": "SE",
        "cities": [
          "Aracaju",
          "Nossa Senhora do Socorro",
          "Lagarto",
          "Itabaiana",
          "São Cristóvão",
          "Estância",
          "Tobias Barreto",
          "Simão Dias",
          "Itabaianinha",
          "Poço Redondo",
          "Nossa Senhora da Glória",
          "Propriá",
          "Barra dos Coqueiros",
          "Laranjeiras",
          "Neópolis"
        ]
      },
      {
        "name": "Tocantins",
        "code": "TO",
        "cities": [
          "Palmas",
          "Araguaína",
          "Gurupi",
          "Porto Nacional",
          "Paraíso do Tocantins",
          "Araguatins",
          "Colinas do Tocantins",
          "Guaraí",
          "Tocantinópolis",
          "Formoso do Araguaia",
          "Dianópolis",
          "Taguatinga",
          "Miracema do Tocantins"
        ]
      }
    ]
  },
  {
    "name": "Afeganistão",
    "code": "AF",
    "states": [
      {
        "name": "Cabul",
        "code": "KAB",
        "cities": [
          "Cabul",
          "Herat",
          "Mazar-i-Sharif",
          "Kandahar",
          "Jalalabad"
        ]
      }
    ]
  },
  {
    "name": "África do Sul",
    "code": "ZA",
    "states": [
      {
        "name": "Gauteng",
        "code": "GP",
        "cities": [
          "Joanesburgo",
          "Pretória",
          "Soweto",
          "Sandton",
          "Midrand",
          "Centurion",
          "Roodepoort",
          "Kempton Park",
          "Benoni",
          "Alberton"
        ]
      },
      {
        "name": "Cabo Ocidental (Western Cape)",
        "code": "WC",
        "cities": [
          "Cidade do Cabo (Cape Town)",
          "Stellenbosch",
          "George",
          "Paarl",
          "Worcester",
          "Mossel Bay",
          "Hermanus"
        ]
      },
      {
        "name": "KwaZulu-Natal",
        "code": "KZN",
        "cities": [
          "Durban",
          "Pietermaritzburg",
          "Pinetown",
          "Newcastle",
          "Richards Bay",
          "Chatsworth",
          "Umhlanga"
        ]
      },
      {
        "name": "Cabo Oriental (Eastern Cape)",
        "code": "EC",
        "cities": [
          "Port Elizabeth (Gqeberha)",
          "East London",
          "Uitenhage",
          "Mthatha",
          "Grahamstown (Makhanda)"
        ]
      },
      {
        "name": "Estado Livre (Free State)",
        "code": "FS",
        "cities": [
          "Bloemfontein",
          "Welkom",
          "Sasolburg",
          "Kroonstad"
        ]
      },
      {
        "name": "Mpumalanga",
        "code": "MP",
        "cities": [
          "Mbombela (Nelspruit)",
          "eMalahleni (Witbank)",
          "Secunda",
          "Middelburg"
        ]
      },
      {
        "name": "Limpopo",
        "code": "LP",
        "cities": [
          "Polokwane",
          "Thohoyandou",
          "Tzaneen",
          "Mokopane"
        ]
      },
      {
        "name": "Noroeste",
        "code": "NW",
        "cities": [
          "Rustenburg",
          "Mahikeng",
          "Potchefstroom",
          "Klerksdorp"
        ]
      },
      {
        "name": "Cabo Setentrional (Northern Cape)",
        "code": "NC",
        "cities": [
          "Kimberley",
          "Upington",
          "Springbok"
        ]
      }
    ]
  },
  {
    "name": "Albânia",
    "code": "AL",
    "states": [
      {
        "name": "Tirana",
        "code": "11",
        "cities": [
          "Tirana",
          "Durrës",
          "Vlorë",
          "Shkodër",
          "Fier",
          "Korçë",
          "Elbasan",
          "Berat",
          "Lushnjë",
          "Pogradec",
          "Gjirokastër"
        ]
      }
    ]
  },
  {
    "name": "Alemanha",
    "code": "DE",
    "states": [
      {
        "name": "Berlim",
        "code": "BE",
        "cities": [
          "Berlim"
        ]
      },
      {
        "name": "Baviera",
        "code": "BY",
        "cities": [
          "Munique",
          "Nuremberg",
          "Augsburgo",
          "Würzburg",
          "Regensburg",
          "Ingolstadt",
          "Fürth",
          "Erlangen",
          "Bamberg",
          "Bayreuth",
          "Landshut",
          "Aschaffenburg",
          "Kempten"
        ]
      },
      {
        "name": "Renânia do Norte-Vestfália",
        "code": "NW",
        "cities": [
          "Colônia",
          "Düsseldorf",
          "Dortmund",
          "Essen",
          "Duisburgo",
          "Bochum",
          "Wuppertal",
          "Bielefeld",
          "Bonn",
          "Münster",
          "Gelsenkirchen",
          "Mönchengladbach",
          "Aachen",
          "Krefeld",
          "Oberhausen",
          "Hagen",
          "Hamm",
          "Mülheim an der Ruhr",
          "Leverkusen",
          "Solingen",
          "Herne",
          "Neuss",
          "Paderborn",
          "Recklinghausen",
          "Bottrop"
        ]
      },
      {
        "name": "Hesse",
        "code": "HE",
        "cities": [
          "Frankfurt am Main",
          "Wiesbaden",
          "Kassel",
          "Darmstadt",
          "Offenbach am Main",
          "Hanau",
          "Gießen",
          "Marburg",
          "Fulda",
          "Rüsselsheim"
        ]
      },
      {
        "name": "Hamburgo",
        "code": "HH",
        "cities": [
          "Hamburgo"
        ]
      },
      {
        "name": "Baden-Württemberg",
        "code": "BW",
        "cities": [
          "Stuttgart",
          "Karlsruhe",
          "Mannheim",
          "Freiburg im Breisgau",
          "Heidelberg",
          "Ulm",
          "Heilbronn",
          "Pforzheim",
          "Reutlingen",
          "Ludwigsburg",
          "Esslingen am Neckar",
          "Tübingen",
          "Konstanz",
          "Aalen",
          "Schwäbisch Gmünd"
        ]
      },
      {
        "name": "Saxônia",
        "code": "SN",
        "cities": [
          "Leipzig",
          "Dresden",
          "Chemnitz",
          "Zwickau",
          "Plauen",
          "Görlitz"
        ]
      },
      {
        "name": "Baixa Saxônia",
        "code": "NI",
        "cities": [
          "Hanôver",
          "Brunsvique (Braunschweig)",
          "Osnabrück",
          "Oldemburgo",
          "Wolfsburg",
          "Göttingen",
          "Salzgitter",
          "Hildesheim",
          "Delmenhorst",
          "Wilhelmshaven",
          "Lüneburg",
          "Celle"
        ]
      },
      {
        "name": "Renânia-Palatinado",
        "code": "RP",
        "cities": [
          "Mogúncia (Mainz)",
          "Ludwigshafen am Rhein",
          "Koblenz",
          "Trier",
          "Kaiserslautern",
          "Worms",
          "Neuwiend"
        ]
      },
      {
        "name": "Schleswig-Holstein",
        "code": "SH",
        "cities": [
          "Kiel",
          "Lübeck",
          "Flensburg",
          "Neumünster",
          "Norderstedt"
        ]
      },
      {
        "name": "Turíngia",
        "code": "TH",
        "cities": [
          "Erfurt",
          "Jena",
          "Gera",
          "Weimar",
          "Gotha",
          "Eisenach"
        ]
      },
      {
        "name": "Brandemburgo",
        "code": "BB",
        "cities": [
          "Potsdam",
          "Cottbus",
          "Brandenburg an der Havel",
          "Frankfurt (Oder)"
        ]
      },
      {
        "name": "Saxônia-Anhalt",
        "code": "ST",
        "cities": [
          "Halle (Saale)",
          "Magdeburgo",
          "Dessau-Roßlau"
        ]
      },
      {
        "name": "Bremen",
        "code": "HB",
        "cities": [
          "Bremen",
          "Bremerhaven"
        ]
      },
      {
        "name": "Meclemburgo-Pomerânia Ocidental",
        "code": "MV",
        "cities": [
          "Rostock",
          "Schwerin",
          "Neubrandenburg",
          "Stralsund",
          "Greifswald"
        ]
      },
      {
        "name": "Sarre",
        "code": "SL",
        "cities": [
          "Saarbrücken",
          "Neunkirchen",
          "Homburg",
          "Völklingen"
        ]
      }
    ]
  },
  {
    "name": "Andorra",
    "code": "AD",
    "states": [
      {
        "name": "Andorra",
        "code": "AD",
        "cities": [
          "Andorra-a-Velha",
          "Escaldes-Engordany",
          "Encamp",
          "Sant Julià de Lòria",
          "La Massana"
        ]
      }
    ]
  },
  {
    "name": "Angola",
    "code": "AO",
    "states": [
      {
        "name": "Luanda",
        "code": "LUA",
        "cities": [
          "Luanda",
          "Viana",
          "Cacuaco",
          "Cazenga",
          "Belas",
          "Talatona",
          "Kilamba Kiaxi",
          "Icolo e Bengo",
          "Quiçama"
        ]
      },
      {
        "name": "Benguela",
        "code": "BGU",
        "cities": [
          "Benguela",
          "Lobito",
          "Catumbela",
          "Baía Farta",
          "Cubal",
          "Ganda"
        ]
      },
      {
        "name": "Huíla",
        "code": "HUI",
        "cities": [
          "Lubango",
          "Matala",
          "Chibia",
          "Quilengues",
          "Humpata"
        ]
      },
      {
        "name": "Huambo",
        "code": "HUA",
        "cities": [
          "Huambo",
          "Caála",
          "Bailundo",
          "Longonjo"
        ]
      },
      {
        "name": "Cabinda",
        "code": "CAB",
        "cities": [
          "Cabinda",
          "Cacongo",
          "Buco-Zau",
          "Belize"
        ]
      },
      {
        "name": "Cuanza Sul",
        "code": "CUS",
        "cities": [
          "Sumbe",
          "Porto Amboim",
          "Gabela",
          "Libolo"
        ]
      },
      {
        "name": "Uíge",
        "code": "UIG",
        "cities": [
          "Uíge",
          "Negage",
          "Sanza Pombo",
          "Maquela do Zombo"
        ]
      },
      {
        "name": "Namibe",
        "code": "NAM",
        "cities": [
          "Moçâmedes (Namibe)",
          "Tômbwa",
          "Bibala"
        ]
      },
      {
        "name": "Malanje",
        "code": "MAL",
        "cities": [
          "Malanje",
          "Calandula",
          "Cangandala"
        ]
      },
      {
        "name": "Zaire",
        "code": "ZAI",
        "cities": [
          "M'banza Kongo",
          "Soyo",
          "Nzeto"
        ]
      },
      {
        "name": "Lunda Norte",
        "code": "LNO",
        "cities": [
          "Dundo",
          "Lucapa",
          "Cambulo"
        ]
      },
      {
        "name": "Lunda Sul",
        "code": "LSU",
        "cities": [
          "Saurimo",
          "Dala",
          "Muconda"
        ]
      },
      {
        "name": "Moxico",
        "code": "MOX",
        "cities": [
          "Luena",
          "Camanongue",
          "Luau"
        ]
      },
      {
        "name": "Bié",
        "code": "BIE",
        "cities": [
          "Kuito",
          "Camacupa",
          "Andulo"
        ]
      },
      {
        "name": "Cunene",
        "code": "CNN",
        "cities": [
          "Ondjiva",
          "Namacunde",
          "Cuanhama"
        ]
      },
      {
        "name": "Cuando Cubango",
        "code": "CCU",
        "cities": [
          "Menongue",
          "Cuito Cuanavale"
        ]
      },
      {
        "name": "Cuanza Norte",
        "code": "CNO",
        "cities": [
          "N'dalatando",
          "Cambambe",
          "Golungo Alto"
        ]
      },
      {
        "name": "Bengo",
        "code": "BGO",
        "cities": [
          "Caxito",
          "Dande",
          "Ambriz"
        ]
      }
    ]
  },
  {
    "name": "Antígua e Barbuda",
    "code": "AG",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Saint John's",
          "All Saints",
          "Liberta",
          "Potters Village"
        ]
      }
    ]
  },
  {
    "name": "Arábia Saudita",
    "code": "SA",
    "states": [
      {
        "name": "Riad (Riyadh)",
        "code": "01",
        "cities": [
          "Riad",
          "Al Kharj",
          "Ad Diriyah",
          "Al Majma'ah"
        ]
      },
      {
        "name": "Meca (Makkah)",
        "code": "02",
        "cities": [
          "Jidá (Jeddah)",
          "Meca",
          "Taif",
          "Rabigh"
        ]
      },
      {
        "name": "Província Oriental (Ash Sharqiyah)",
        "code": "04",
        "cities": [
          "Dammam",
          "Khobar",
          "Dhahran",
          "Jubail",
          "Al Ahsa (Hofuf)",
          "Qatif",
          "Hafar Al Batin"
        ]
      },
      {
        "name": "Medina (Al Madinah)",
        "code": "03",
        "cities": [
          "Medina",
          "Yanbu",
          "Al Ula"
        ]
      },
      {
        "name": "Asir",
        "code": "14",
        "cities": [
          "Abha",
          "Khamis Mushait"
        ]
      },
      {
        "name": "Al Qassim",
        "code": "05",
        "cities": [
          "Buraidah",
          "Unaizah"
        ]
      },
      {
        "name": "Tabuk",
        "code": "07",
        "cities": [
          "Tabuk",
          "NEOM"
        ]
      }
    ]
  },
  {
    "name": "Argélia",
    "code": "DZ",
    "states": [
      {
        "name": "Argel",
        "code": "16",
        "cities": [
          "Argel",
          "Bab Ezzouar",
          "Sidi M'Hamed",
          "Kouba",
          "El Harrach"
        ]
      },
      {
        "name": "Orã (Oran)",
        "code": "31",
        "cities": [
          "Orã",
          "Es Sénia",
          "Bir El Djir",
          "Arzew"
        ]
      },
      {
        "name": "Constantina",
        "code": "25",
        "cities": [
          "Constantina",
          "El Khroub",
          "Hamma Bouziane"
        ]
      },
      {
        "name": "Annaba",
        "code": "23",
        "cities": [
          "Annaba",
          "El Bouni",
          "Sidi Amar"
        ]
      },
      {
        "name": "Blida",
        "code": "09",
        "cities": [
          "Blida",
          "Boufarik",
          "Ouled Yaïch"
        ]
      },
      {
        "name": "Sétif",
        "code": "19",
        "cities": [
          "Sétif",
          "El Eulma",
          "Aïn Oulmene"
        ]
      }
    ]
  },
  {
    "name": "Argentina",
    "code": "AR",
    "states": [
      {
        "name": "Buenos Aires",
        "code": "BA",
        "cities": [
          "Cidade de Buenos Aires (CABA)",
          "La Plata",
          "Mar del Plata",
          "Bahía Blanca",
          "Quilmes",
          "Lanús",
          "Morón",
          "San Isidro",
          "Lomas de Zamora",
          "Tandil",
          "Vicente López",
          "Tigre",
          "San Nicolás",
          "Pergamino",
          "Olavarría"
        ]
      },
      {
        "name": "Córdoba",
        "code": "CBA",
        "cities": [
          "Córdoba",
          "Villa Carlos Paz",
          "Río Cuarto",
          "Villa María",
          "San Francisco",
          "Alta Gracia",
          "Bell Ville",
          "Río Tercero"
        ]
      },
      {
        "name": "Santa Fe",
        "code": "SF",
        "cities": [
          "Rosario",
          "Santa Fe",
          "Rafaela",
          "Venado Tuerto",
          "Reconquista",
          "Santo Tomé",
          "Villa Gobernador Gálvez",
          "Esperanza"
        ]
      },
      {
        "name": "Mendoza",
        "code": "MDZ",
        "cities": [
          "Mendoza",
          "San Rafael",
          "Godoy Cruz",
          "Guaymallén",
          "Las Heras",
          "Luján de Cuyo",
          "Maipú",
          "San Martín"
        ]
      },
      {
        "name": "Tucumán",
        "code": "TUC",
        "cities": [
          "San Miguel de Tucumán",
          "Yerba Buena",
          "Tafí Viejo",
          "Concepción",
          "Banda del Río Salí",
          "Aguilares"
        ]
      },
      {
        "name": "Entre Ríos",
        "code": "ER",
        "cities": [
          "Paraná",
          "Concordia",
          "Gualeguaychú",
          "Concepción del Uruguay",
          "Gualeguay",
          "Villaguay",
          "Chajarí"
        ]
      },
      {
        "name": "Salta",
        "code": "SLA",
        "cities": [
          "Salta",
          "San Ramón de la Nueva Orán",
          "Tartagal",
          "General Güemes",
          "Cafayate"
        ]
      },
      {
        "name": "Neuquén",
        "code": "NQN",
        "cities": [
          "Neuquén",
          "San Martín de los Andes",
          "Cutral Có",
          "Plottier",
          "Centenario",
          "Zapala",
          "Villa La Angostura"
        ]
      },
      {
        "name": "Río Negro",
        "code": "RN",
        "cities": [
          "Bariloche",
          "Cipolletti",
          "General Roca",
          "Viedma",
          "Villa Regina",
          "Cinco Saltos"
        ]
      },
      {
        "name": "Chaco",
        "code": "CH",
        "cities": [
          "Resistencia",
          "Presidencia Roque Sáenz Peña",
          "Villa Ángela",
          "Barranqueras"
        ]
      },
      {
        "name": "Corrientes",
        "code": "CR",
        "cities": [
          "Corrientes",
          "Goya",
          "Paso de los Libres",
          "Curuzú Cuatiá",
          "Mercedes"
        ]
      },
      {
        "name": "Misiones",
        "code": "MI",
        "cities": [
          "Posadas",
          "Oberá",
          "Eldorado",
          "Puerto Iguazú",
          "Apóstoles"
        ]
      },
      {
        "name": "San Juan",
        "code": "SJ",
        "cities": [
          "San Juan",
          "Rawson",
          "Rivadavia",
          "Chimbas",
          "Santa Lucía"
        ]
      },
      {
        "name": "Jujuy",
        "code": "JU",
        "cities": [
          "San Salvador de Jujuy",
          "Palpalá",
          "San Pedro",
          "Libertador General San Martín"
        ]
      },
      {
        "name": "Chubut",
        "code": "CHB",
        "cities": [
          "Comodoro Rivadavia",
          "Trelew",
          "Puerto Madryn",
          "Esquel",
          "Rawson"
        ]
      },
      {
        "name": "San Luis",
        "code": "SL",
        "cities": [
          "San Luis",
          "Villa Mercedes",
          "Merlo"
        ]
      },
      {
        "name": "Santiago del Estero",
        "code": "SE",
        "cities": [
          "Santiago del Estero",
          "La Banda",
          "Termas de Río Hondo"
        ]
      },
      {
        "name": "Catamarca",
        "code": "CAT",
        "cities": [
          "San Fernando del Valle de Catamarca",
          "Valle Viejo",
          "Andalgalá"
        ]
      },
      {
        "name": "La Pampa",
        "code": "LP",
        "cities": [
          "Santa Rosa",
          "General Pico",
          "Toay"
        ]
      },
      {
        "name": "La Rioja",
        "code": "LR",
        "cities": [
          "La Rioja",
          "Chilecito",
          "Aimogasta"
        ]
      },
      {
        "name": "Formosa",
        "code": "FO",
        "cities": [
          "Formosa",
          "Clorinda",
          "Pirané"
        ]
      },
      {
        "name": "Santa Cruz",
        "code": "SC",
        "cities": [
          "Río Gallegos",
          "Caleta Olivia",
          "El Calafate",
          "Pico Truncado"
        ]
      },
      {
        "name": "Tierra del Fuego",
        "code": "TF",
        "cities": [
          "Ushuaia",
          "Río Grande",
          "Tolhuin"
        ]
      }
    ]
  },
  {
    "name": "Armênia",
    "code": "AM",
    "states": [
      {
        "name": "Erevan",
        "code": "ER",
        "cities": [
          "Erevan",
          "Gyumri",
          "Vanadzor",
          "Vagharshapat",
          "Abovyan",
          "Kapan"
        ]
      }
    ]
  },
  {
    "name": "Austrália",
    "code": "AU",
    "states": [
      {
        "name": "Nova Gales do Sul",
        "code": "NSW",
        "cities": [
          "Sydney",
          "Newcastle",
          "Central Coast",
          "Wollongong",
          "Maitland",
          "Tweed Heads",
          "Wagga Wagga",
          "Albury",
          "Coffs Harbour",
          "Port Macquarie",
          "Orange",
          "Dubbo",
          "Tamworth",
          "Bathurst"
        ]
      },
      {
        "name": "Vitória",
        "code": "VIC",
        "cities": [
          "Melbourne",
          "Geelong",
          "Ballarat",
          "Bendigo",
          "Shepparton",
          "Mildura",
          "Warrnambool",
          "Wodonga",
          "Traralgon",
          "Wangarratta"
        ]
      },
      {
        "name": "Queensland",
        "code": "QLD",
        "cities": [
          "Brisbane",
          "Gold Coast",
          "Sunshine Coast",
          "Townsville",
          "Cairns",
          "Toowoomba",
          "Mackay",
          "Rockhampton",
          "Hervey Bay",
          "Bundaberg",
          "Gladstone"
        ]
      },
      {
        "name": "Austrália Ocidental",
        "code": "WA",
        "cities": [
          "Perth",
          "Mandurah",
          "Bunbury",
          "Geraldton",
          "Kalgoorlie",
          "Albany",
          "Busselton",
          "Karratha",
          "Broome"
        ]
      },
      {
        "name": "Austrália Meridional",
        "code": "SA",
        "cities": [
          "Adelaide",
          "Mount Gambier",
          "Whyalla",
          "Gawler",
          "Murray Bridge",
          "Port Lincoln",
          "Port Augusta"
        ]
      },
      {
        "name": "Tasmânia",
        "code": "TAS",
        "cities": [
          "Hobart",
          "Launceston",
          "Devonport",
          "Burnie",
          "Kingston"
        ]
      },
      {
        "name": "Território da Capital Australiana",
        "code": "ACT",
        "cities": [
          "Canberra",
          "Queanbeyan"
        ]
      },
      {
        "name": "Território do Norte",
        "code": "NT",
        "cities": [
          "Darwin",
          "Palmerston",
          "Alice Springs",
          "Katherine"
        ]
      }
    ]
  },
  {
    "name": "Áustria",
    "code": "AT",
    "states": [
      {
        "name": "Viena",
        "code": "W",
        "cities": [
          "Viena"
        ]
      },
      {
        "name": "Estíria",
        "code": "ST",
        "cities": [
          "Graz",
          "Leoben",
          "Kapfenberg",
          "Bruck an der Mur"
        ]
      },
      {
        "name": "Alta Áustria",
        "code": "OO",
        "cities": [
          "Linz",
          "Wels",
          "Steyr",
          "Traun",
          "Leonding"
        ]
      },
      {
        "name": "Salzburgo",
        "code": "SB",
        "cities": [
          "Salzburgo",
          "Hallein",
          "Saalfelden",
          "Sankt Johann im Pongau"
        ]
      },
      {
        "name": "Tirol",
        "code": "TR",
        "cities": [
          "Innsbruck",
          "Kufstein",
          "Telfs",
          "Schwaz",
          "Hall in Tirol"
        ]
      },
      {
        "name": "Caríntia",
        "code": "KT",
        "cities": [
          "Klagenfurt",
          "Villach",
          "Wolfsberg",
          "Spittal an der Drau"
        ]
      },
      {
        "name": "Baixa Áustria",
        "code": "NO",
        "cities": [
          "Sankt Pölten",
          "Wiener Neustadt",
          "Klosterneuburg",
          "Baden",
          "Krems an der Donau"
        ]
      },
      {
        "name": "Vorarlberg",
        "code": "VB",
        "cities": [
          "Dornbirn",
          "Feldkirch",
          "Bregenz",
          "Lustenau"
        ]
      },
      {
        "name": "Burgenland",
        "code": "BG",
        "cities": [
          "Eisenstadt",
          "Oberwart",
          "Mattersburg"
        ]
      }
    ]
  },
  {
    "name": "Azerbaijão",
    "code": "AZ",
    "states": [
      {
        "name": "Baku",
        "code": "BA",
        "cities": [
          "Baku",
          "Sumqayit",
          "Ganja",
          "Mingachevir",
          "Lankaran",
          "Shirvan",
          "Nakhchivan"
        ]
      }
    ]
  },
  {
    "name": "Bahamas",
    "code": "BS",
    "states": [
      {
        "name": "Bahamas",
        "code": "BS",
        "cities": [
          "Nassau",
          "Freeport",
          "West End",
          "Coopers Town"
        ]
      }
    ]
  },
  {
    "name": "Bahrein",
    "code": "BH",
    "states": [
      {
        "name": "Bahrein",
        "code": "BH",
        "cities": [
          "Manama",
          "Riffa",
          "Muharraq",
          "Hamad Town",
          "A'ali",
          "Sitra"
        ]
      }
    ]
  },
  {
    "name": "Bangladesh",
    "code": "BD",
    "states": [
      {
        "name": "Dhaka",
        "code": "13",
        "cities": [
          "Dhaka",
          "Gazipur",
          "Narayanganj",
          "Tangail",
          "Savar"
        ]
      },
      {
        "name": "Chittagong",
        "code": "20",
        "cities": [
          "Chittagong (Chattogram)",
          "Comilla",
          "Cox's Bazar",
          "Brahmanbaria"
        ]
      },
      {
        "name": "Khulna",
        "code": "27",
        "cities": [
          "Khulna",
          "Jessore",
          "Kushtia"
        ]
      },
      {
        "name": "Rajshahi",
        "code": "54",
        "cities": [
          "Rajshahi",
          "Bogra",
          "Pabna"
        ]
      },
      {
        "name": "Sylhet",
        "code": "60",
        "cities": [
          "Sylhet",
          "Moulvibazar",
          "Habiganj"
        ]
      }
    ]
  },
  {
    "name": "Barbados",
    "code": "BB",
    "states": [
      {
        "name": "Barbados",
        "code": "BB",
        "cities": [
          "Bridgetown",
          "Speightstown",
          "Oistins",
          "Holetown"
        ]
      }
    ]
  },
  {
    "name": "Bélgica",
    "code": "BE",
    "states": [
      {
        "name": "Bruxelas",
        "code": "BRU",
        "cities": [
          "Bruxelas",
          "Schaerbeek",
          "Anderlecht",
          "Molenbeek-Saint-Jean",
          "Ixelles",
          "Uccle"
        ]
      },
      {
        "name": "Flandres",
        "code": "VLG",
        "cities": [
          "Antuérpia",
          "Gante",
          "Bruges",
          "Leuven",
          "Aalst",
          "Mechelen",
          "Kortrijk",
          "Hasselt",
          "Sint-Niklaas",
          "Oostende",
          "Genk",
          "Roeselare"
        ]
      },
      {
        "name": "Valônia",
        "code": "WAL",
        "cities": [
          "Charleroi",
          "Liège",
          "Namur",
          "Mons",
          "La Louvière",
          "Tournai",
          "Seraing",
          "Verviers",
          "Mouscron"
        ]
      }
    ]
  },
  {
    "name": "Belize",
    "code": "BZ",
    "states": [
      {
        "name": "Belize",
        "code": "BZ",
        "cities": [
          "Cidade de Belize",
          "Belmopan",
          "San Ignacio",
          "Orange Walk Town",
          "San Pedro"
        ]
      }
    ]
  },
  {
    "name": "Benim",
    "code": "BJ",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Porto-Novo",
          "Cotonou",
          "Abomey-Calavi",
          "Parakou",
          "Djougou"
        ]
      }
    ]
  },
  {
    "name": "Bermudas",
    "code": "BM",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Hamilton",
          "St. George's",
          "Somerset"
        ]
      }
    ]
  },
  {
    "name": "Bielorrússia",
    "code": "BY",
    "states": [
      {
        "name": "Minsk",
        "code": "MI",
        "cities": [
          "Minsk",
          "Barysaw",
          "Salihorsk",
          "Maladzyechna",
          "Zhodzina",
          "Slutsk"
        ]
      },
      {
        "name": "Gomel",
        "code": "HO",
        "cities": [
          "Gomel",
          "Mazyr",
          "Zhlobin",
          "Svyetlahorsk",
          "Rechytsa"
        ]
      },
      {
        "name": "Mogilev",
        "code": "MA",
        "cities": [
          "Mogilev",
          "Babruysk",
          "Asipovichy",
          "Horki"
        ]
      },
      {
        "name": "Vitebsk",
        "code": "VI",
        "cities": [
          "Vitebsk",
          "Orsha",
          "Navapolatsk",
          "Polatsk"
        ]
      },
      {
        "name": "Grodno",
        "code": "HR",
        "cities": [
          "Grodno",
          "Lida",
          "Slonim",
          "Vawkavysk"
        ]
      },
      {
        "name": "Brest",
        "code": "BR",
        "cities": [
          "Brest",
          "Baranavichy",
          "Pinsk",
          "Kobryn",
          "Byaroza"
        ]
      }
    ]
  },
  {
    "name": "Bolívia",
    "code": "BO",
    "states": [
      {
        "name": "La Paz",
        "code": "LP",
        "cities": [
          "La Paz",
          "El Alto",
          "Viacha",
          "Achocalla"
        ]
      },
      {
        "name": "Santa Cruz",
        "code": "SC",
        "cities": [
          "Santa Cruz de la Sierra",
          "Montero",
          "Warnes",
          "La Guardia",
          "Cotoca"
        ]
      },
      {
        "name": "Cochabamba",
        "code": "CB",
        "cities": [
          "Cochabamba",
          "Quillacollo",
          "Sacaba",
          "Tiquipaya",
          "Colcapirhua"
        ]
      },
      {
        "name": "Chuquisaca",
        "code": "CH",
        "cities": [
          "Sucre",
          "Monteagudo",
          "Tarabuco"
        ]
      },
      {
        "name": "Oruro",
        "code": "OR",
        "cities": [
          "Oruro",
          "Huanuni",
          "Challapata"
        ]
      },
      {
        "name": "Potosí",
        "code": "PO",
        "cities": [
          "Potosí",
          "Llallagua",
          "Villazón",
          "Tupiza",
          "Uyuni"
        ]
      },
      {
        "name": "Tarija",
        "code": "TJ",
        "cities": [
          "Tarija",
          "Yacuiba",
          "Bermejo",
          "Villamontes"
        ]
      },
      {
        "name": "Beni",
        "code": "BE",
        "cities": [
          "Trinidad",
          "Riberalta",
          "Guayaramerín",
          "San Borja"
        ]
      },
      {
        "name": "Pando",
        "code": "PA",
        "cities": [
          "Cobija",
          "Porvenir",
          "Puerto Rico"
        ]
      }
    ]
  },
  {
    "name": "Bósnia e Herzegovina",
    "code": "BA",
    "states": [
      {
        "name": "Federação da Bósnia e Herzegovina",
        "code": "BIH",
        "cities": [
          "Sarajevo",
          "Tuzla",
          "Zenica",
          "Mostar",
          "Bihać",
          "Travnik"
        ]
      },
      {
        "name": "República Sérvia (Republika Srpska)",
        "code": "SRP",
        "cities": [
          "Banja Luka",
          "Bijeljina",
          "Prijedor",
          "Doboj",
          "Trebinje"
        ]
      },
      {
        "name": "Distrito de Brčko",
        "code": "BRC",
        "cities": [
          "Brčko"
        ]
      }
    ]
  },
  {
    "name": "Botsuana",
    "code": "BW",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Gaborone",
          "Francistown",
          "Molepolole",
          "Maun",
          "Serowe"
        ]
      }
    ]
  },
  {
    "name": "Brunei",
    "code": "BN",
    "states": [
      {
        "name": "Brunei e Muara",
        "code": "BM",
        "cities": [
          "Bandar Seri Begawan",
          "Kuala Belait",
          "Seria",
          "Tutong"
        ]
      }
    ]
  },
  {
    "name": "Bulgária",
    "code": "BG",
    "states": [
      {
        "name": "Sófia (Cidade)",
        "code": "22",
        "cities": [
          "Sófia"
        ]
      },
      {
        "name": "Plovdiv",
        "code": "16",
        "cities": [
          "Plovdiv",
          "Asenovgrad",
          "Karlovo"
        ]
      },
      {
        "name": "Varna",
        "code": "03",
        "cities": [
          "Varna",
          "Provadiya",
          "Devnya"
        ]
      },
      {
        "name": "Burgas",
        "code": "02",
        "cities": [
          "Burgas",
          "Nesebar",
          "Pomorie",
          "Karnobat"
        ]
      },
      {
        "name": "Ruse",
        "code": "18",
        "cities": [
          "Ruse",
          "Byala"
        ]
      },
      {
        "name": "Stara Zagora",
        "code": "24",
        "cities": [
          "Stara Zagora",
          "Kazanlak",
          "Chirpan"
        ]
      },
      {
        "name": "Pleven",
        "code": "15",
        "cities": [
          "Pleven",
          "Cherven Bryag"
        ]
      },
      {
        "name": "Sliven",
        "code": "20",
        "cities": [
          "Sliven",
          "Nova Zagora"
        ]
      },
      {
        "name": "Dobrich",
        "code": "08",
        "cities": [
          "Dobrich",
          "Balchik",
          "Kavarna"
        ]
      },
      {
        "name": "Shumen",
        "code": "27",
        "cities": [
          "Shumen",
          "Novi Pazar"
        ]
      },
      {
        "name": "Pernik",
        "code": "14",
        "cities": [
          "Pernik",
          "Radomir"
        ]
      },
      {
        "name": "Haskovo",
        "code": "26",
        "cities": [
          "Haskovo",
          "Dimitrovgrad",
          "Svilengrad"
        ]
      },
      {
        "name": "Veliko Tarnovo",
        "code": "04",
        "cities": [
          "Veliko Tarnovo",
          "Gorna Oryahovitsa",
          "Svishtov"
        ]
      },
      {
        "name": "Blagoevgrad",
        "code": "01",
        "cities": [
          "Blagoevgrad",
          "Petrich",
          "Sandanski",
          "Gotse Delchev",
          "Bansko"
        ]
      }
    ]
  },
  {
    "name": "Burquina Faso",
    "code": "BF",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Uagadugu",
          "Bobo-Dioulasso",
          "Koudougou",
          "Ouahigouya",
          "Banfora"
        ]
      }
    ]
  },
  {
    "name": "Burundi",
    "code": "BI",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Gitega",
          "Bujumbura",
          "Ngozi",
          "Rumonge"
        ]
      }
    ]
  },
  {
    "name": "Butão",
    "code": "BT",
    "states": [
      {
        "name": "Thimphu",
        "code": "15",
        "cities": [
          "Thimphu",
          "Phuntsholing",
          "Paro",
          "Gelephu"
        ]
      }
    ]
  },
  {
    "name": "Cabo Verde",
    "code": "CV",
    "states": [
      {
        "name": "Santiago",
        "code": "ST",
        "cities": [
          "Praia",
          "Assomada (Santa Catarina)",
          "Tarrafal",
          "Pedra Badejo (Santa Cruz)",
          "Cidade Velha"
        ]
      },
      {
        "name": "São Vicente",
        "code": "SV",
        "cities": [
          "Mindelo"
        ]
      },
      {
        "name": "Sal",
        "code": "SL",
        "cities": [
          "Espargos",
          "Santa Maria"
        ]
      },
      {
        "name": "Boa Vista",
        "code": "BV",
        "cities": [
          "Sal Rei"
        ]
      },
      {
        "name": "Santo Antão",
        "code": "SA",
        "cities": [
          "Porto Novo",
          "Ribeira Grande",
          "Ponta do Sol"
        ]
      },
      {
        "name": "Fogo",
        "code": "FG",
        "cities": [
          "São Filipe",
          "Mosteiros"
        ]
      },
      {
        "name": "São Nicolau",
        "code": "SN",
        "cities": [
          "Ribeira Brava",
          "Tarrafal de São Nicolau"
        ]
      },
      {
        "name": "Maio",
        "code": "MA",
        "cities": [
          "Vila do Maio (Porto Inglês)"
        ]
      },
      {
        "name": "Brava",
        "code": "BR",
        "cities": [
          "Nova Sintra"
        ]
      }
    ]
  },
  {
    "name": "Camarões",
    "code": "CM",
    "states": [
      {
        "name": "Centre",
        "code": "CE",
        "cities": [
          "Yaoundé",
          "Mbalmayo",
          "Bafia"
        ]
      },
      {
        "name": "Littoral",
        "code": "LT",
        "cities": [
          "Douala",
          "Edéa",
          "Nkongsamba"
        ]
      },
      {
        "name": "Ouest",
        "code": "OU",
        "cities": [
          "Bafoussam",
          "Foumban",
          "Dschang"
        ]
      },
      {
        "name": "Nord-Ouest",
        "code": "NW",
        "cities": [
          "Bamenda"
        ]
      },
      {
        "name": "Sud-Ouest",
        "code": "SW",
        "cities": [
          "Buea",
          "Limbe",
          "Kumba"
        ]
      }
    ]
  },
  {
    "name": "Camboja",
    "code": "KH",
    "states": [
      {
        "name": "Phnom Penh",
        "code": "12",
        "cities": [
          "Phnom Penh",
          "Siem Reap",
          "Battambang",
          "Sihanoukville",
          "Poipet",
          "Kampong Cham"
        ]
      }
    ]
  },
  {
    "name": "Canadá",
    "code": "CA",
    "states": [
      {
        "name": "Ontário",
        "code": "ON",
        "cities": [
          "Toronto",
          "Ottawa",
          "Mississauga",
          "Brampton",
          "Hamilton",
          "London",
          "Markham",
          "Vaughan",
          "Kitchener",
          "Windsor",
          "Richmond Hill",
          "Oakville",
          "Burlington",
          "Greater Sudbury",
          "Oshawa",
          "Barrie",
          "St. Catharines",
          "Cambridge",
          "Kingston",
          "Guelph",
          "Thunder Bay",
          "Waterloo",
          "Brantford",
          "Pickering",
          "Niagara Falls"
        ]
      },
      {
        "name": "Quebec",
        "code": "QC",
        "cities": [
          "Montreal",
          "Cidade de Quebec",
          "Laval",
          "Gatineau",
          "Longueuil",
          "Sherbrooke",
          "Saguenay",
          "Lévis",
          "Trois-Rivières",
          "Terrebonne",
          "Saint-Jean-sur-Richelieu",
          "Brossard",
          "Repentigny",
          "Drummondville",
          "Saint-Jérôme"
        ]
      },
      {
        "name": "Colúmbia Britânica",
        "code": "BC",
        "cities": [
          "Vancouver",
          "Surrey",
          "Burnaby",
          "Richmond",
          "Abbotsford",
          "Coquitlam",
          "Kelowna",
          "Langley",
          "Saanich",
          "Delta",
          "Victoria",
          "Kamloops",
          "Nanaimo",
          "Chilliwack",
          "Prince George",
          "Vernon",
          "Penticton"
        ]
      },
      {
        "name": "Alberta",
        "code": "AB",
        "cities": [
          "Calgary",
          "Edmonton",
          "Red Deer",
          "Lethbridge",
          "St. Albert",
          "Medicine Hat",
          "Grande Prairie",
          "Airdrie",
          "Spruce Grove",
          "Leduc",
          "Fort McMurray",
          "Banff"
        ]
      },
      {
        "name": "Manitoba",
        "code": "MB",
        "cities": [
          "Winnipeg",
          "Brandon",
          "Steinbach",
          "Thompson",
          "Portage la Prairie",
          "Winkler",
          "Selkirk"
        ]
      },
      {
        "name": "Saskatchewan",
        "code": "SK",
        "cities": [
          "Saskatoon",
          "Regina",
          "Prince Albert",
          "Moose Jaw",
          "Swift Current",
          "Yorkton",
          "North Battleford"
        ]
      },
      {
        "name": "Nova Escócia",
        "code": "NS",
        "cities": [
          "Halifax",
          "Dartmouth",
          "Sydney",
          "Truro",
          "New Glasgow",
          "Glace Bay",
          "Kentville"
        ]
      },
      {
        "name": "Novo Brunswick",
        "code": "NB",
        "cities": [
          "Moncton",
          "Saint John",
          "Fredericton",
          "Dieppe",
          "Miramichi",
          "Edmundston",
          "Bathurst"
        ]
      },
      {
        "name": "Terra Nova e Labrador",
        "code": "NL",
        "cities": [
          "St. John's",
          "Mount Pearl",
          "Corner Brook",
          "Conception Bay South",
          "Grand Falls-Windsor"
        ]
      },
      {
        "name": "Ilha do Príncipe Eduardo",
        "code": "PE",
        "cities": [
          "Charlottetown",
          "Summerside",
          "Stratford",
          "Cornwall"
        ]
      }
    ]
  },
  {
    "name": "Catar",
    "code": "QA",
    "states": [
      {
        "name": "Doha",
        "code": "DA",
        "cities": [
          "Doha",
          "Al Rayyan",
          "Al Wakrah",
          "Lusail",
          "Al Khor",
          "Umm Salal",
          "Al Daayen",
          "Madinat ash Shamal"
        ]
      }
    ]
  },
  {
    "name": "Cazaquistão",
    "code": "KZ",
    "states": [
      {
        "name": "Almaty (Cidade)",
        "code": "ALA",
        "cities": [
          "Almaty"
        ]
      },
      {
        "name": "Astana (Capital)",
        "code": "AST",
        "cities": [
          "Astana"
        ]
      },
      {
        "name": "Shymkent (Cidade)",
        "code": "SHY",
        "cities": [
          "Shymkent"
        ]
      },
      {
        "name": "Karaganda",
        "code": "KAR",
        "cities": [
          "Karaganda",
          "Temirtau",
          "Balkhash"
        ]
      },
      {
        "name": "Aktobe",
        "code": "AKT",
        "cities": [
          "Aktobe",
          "Khromtau"
        ]
      },
      {
        "name": "Atyrau",
        "code": "ATY",
        "cities": [
          "Atyrau",
          "Kulsary"
        ]
      },
      {
        "name": "Pavlodar",
        "code": "PAV",
        "cities": [
          "Pavlodar",
          "Ekibastuz",
          "Aksu"
        ]
      },
      {
        "name": "Cazaquistão Oriental",
        "code": "VOS",
        "cities": [
          "Oskemen (Ust-Kamenogorsk)",
          "Semey (Semipalatinsk)"
        ]
      },
      {
        "name": "Kostanay",
        "code": "KUS",
        "cities": [
          "Kostanay",
          "Rudny",
          "Arkalyk"
        ]
      }
    ]
  },
  {
    "name": "Chade",
    "code": "TD",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "N'Djamena",
          "Moundou",
          "Sarh",
          "Abéché",
          "Kélo"
        ]
      }
    ]
  },
  {
    "name": "Chile",
    "code": "CL",
    "states": [
      {
        "name": "Región Metropolitana",
        "code": "RM",
        "cities": [
          "Santiago",
          "Puente Alto",
          "Maipú",
          "La Florida",
          "Las Condes",
          "San Bernardo",
          "Providencia",
          "Ñuñoa",
          "Peñalolén",
          "Pudahuel",
          "Quilicura",
          "Estación Central"
        ]
      },
      {
        "name": "Valparaíso",
        "code": "VS",
        "cities": [
          "Valparaíso",
          "Viña del Mar",
          "Quilpué",
          "Villa Alemana",
          "Quillota",
          "San Antonio",
          "San Felipe",
          "Los Andes",
          "Concón"
        ]
      },
      {
        "name": "Biobío",
        "code": "BI",
        "cities": [
          "Concepción",
          "Talcahuano",
          "Los Ángeles",
          "San Pedro de la Paz",
          "Coronel",
          "Chiguayante",
          "Hualpén",
          "Tomé"
        ]
      },
      {
        "name": "Antofagasta",
        "code": "AN",
        "cities": [
          "Antofagasta",
          "Calama",
          "Tocopilla",
          "Mejillones"
        ]
      },
      {
        "name": "Coquimbo",
        "code": "CO",
        "cities": [
          "La Serena",
          "Coquimbo",
          "Ovalle",
          "Illapel"
        ]
      },
      {
        "name": "Araucanía",
        "code": "AR",
        "cities": [
          "Temuco",
          "Padre Las Casas",
          "Villarrica",
          "Angol",
          "Pucón",
          "Victoria"
        ]
      },
      {
        "name": "Los Lagos",
        "code": "LL",
        "cities": [
          "Puerto Montt",
          "Osorno",
          "Castro",
          "Ancud",
          "Puerto Varas"
        ]
      },
      {
        "name": "Maule",
        "code": "ML",
        "cities": [
          "Talca",
          "Curicó",
          "Linares",
          "Constitución",
          "Cauquenes"
        ]
      },
      {
        "name": "O'Higgins",
        "code": "LI",
        "cities": [
          "Rancagua",
          "San Fernando",
          "Rengo",
          "Machalí"
        ]
      },
      {
        "name": "Tarapacá",
        "code": "TA",
        "cities": [
          "Iquique",
          "Alto Hospicio",
          "Pozo Almonte"
        ]
      },
      {
        "name": "Arica y Parinacota",
        "code": "AP",
        "cities": [
          "Arica",
          "Putre"
        ]
      },
      {
        "name": "Los Ríos",
        "code": "LR",
        "cities": [
          "Valdivia",
          "La Unión",
          "Río Bueno",
          "Panguipulli"
        ]
      },
      {
        "name": "Ñuble",
        "code": "NB",
        "cities": [
          "Chillán",
          "San Carlos",
          "Chillán Viejo"
        ]
      },
      {
        "name": "Atacama",
        "code": "AT",
        "cities": [
          "Copiapó",
          "Vallenar",
          "Caldera",
          "Chañaral"
        ]
      },
      {
        "name": "Magallanes",
        "code": "MA",
        "cities": [
          "Punta Arenas",
          "Puerto Natales",
          "Porvenir"
        ]
      },
      {
        "name": "Aysén",
        "code": "AI",
        "cities": [
          "Coyhaique",
          "Puerto Aysén"
        ]
      }
    ]
  },
  {
    "name": "China",
    "code": "CN",
    "states": [
      {
        "name": "Pequim (Beijing)",
        "code": "BJ",
        "cities": [
          "Pequim (Beijing)",
          "Chaoyang",
          "Haidian",
          "Xicheng",
          "Dongcheng",
          "Fengtai"
        ]
      },
      {
        "name": "Xangai (Shanghai)",
        "code": "SH",
        "cities": [
          "Xangai (Shanghai)",
          "Pudong",
          "Minhang",
          "Huangpu",
          "Xuhui",
          "Jing'an"
        ]
      },
      {
        "name": "Guangdong",
        "code": "GD",
        "cities": [
          "Cantão (Guangzhou)",
          "Shenzhen",
          "Dongguan",
          "Foshan",
          "Zhongshan",
          "Zhuhai",
          "Huizhou",
          "Jiangmen",
          "Shantou"
        ]
      },
      {
        "name": "Zhejiang",
        "code": "ZJ",
        "cities": [
          "Hangzhou",
          "Ningbo",
          "Wenzhou",
          "Shaoxing",
          "Jiaxing",
          "Jinhua",
          "Taizhou"
        ]
      },
      {
        "name": "Jiangsu",
        "code": "JS",
        "cities": [
          "Nanquim (Nanjing)",
          "Suzhou",
          "Wuxi",
          "Changzhou",
          "Nantong",
          "Xuzhou",
          "Yangzhou",
          "Yancheng"
        ]
      },
      {
        "name": "Sichuan",
        "code": "SC",
        "cities": [
          "Chengdu",
          "Mianyang",
          "Nanchong",
          "Yibin",
          "Luzhou",
          "Deyang"
        ]
      },
      {
        "name": "Hubei",
        "code": "HB",
        "cities": [
          "Wuhan",
          "Xiangyang",
          "Yichang",
          "Jingzhou",
          "Huangshi"
        ]
      },
      {
        "name": "Shaanxi",
        "code": "SN",
        "cities": [
          "Xi'an",
          "Baoji",
          "Xianyang",
          "Weinan",
          "Hanzhong"
        ]
      },
      {
        "name": "Shandong",
        "code": "SD",
        "cities": [
          "Jinan",
          "Qingdao",
          "Yantai",
          "Weifang",
          "Zibo",
          "Jining",
          "Linyi"
        ]
      },
      {
        "name": "Hunan",
        "code": "HN",
        "cities": [
          "Changsha",
          "Zhuzhou",
          "Xiangtan",
          "Hengyang",
          "Yueyang"
        ]
      },
      {
        "name": "Fujian",
        "code": "FJ",
        "cities": [
          "Fuzhou",
          "Xiamen",
          "Quanzhou",
          "Zhangzhou",
          "Putian"
        ]
      },
      {
        "name": "Henan",
        "code": "HA",
        "cities": [
          "Zhengzhou",
          "Luoyang",
          "Nanyang",
          "Kaifeng",
          "Xinxiang"
        ]
      },
      {
        "name": "Liaoning",
        "code": "LN",
        "cities": [
          "Shenyang",
          "Dalian",
          "Anshan",
          "Fushun",
          "Jinzhou"
        ]
      },
      {
        "name": "Chongqing",
        "code": "CQ",
        "cities": [
          "Chongqing",
          "Yuzhong",
          "Jiangbei",
          "Shapingba",
          "Jiulongpo"
        ]
      },
      {
        "name": "Tianjin",
        "code": "TJ",
        "cities": [
          "Tianjin",
          "Binhai",
          "Nankai",
          "Heping"
        ]
      }
    ]
  },
  {
    "name": "Chipre",
    "code": "CY",
    "states": [
      {
        "name": "Chipre",
        "code": "CY",
        "cities": [
          "Nicósia",
          "Limassol",
          "Lárnaca",
          "Pafos",
          "Famagusta",
          "Ayia Napa",
          "Kyrenia"
        ]
      }
    ]
  },
  {
    "name": "Colômbia",
    "code": "CO",
    "states": [
      {
        "name": "Bogotá D.C.",
        "code": "DC",
        "cities": [
          "Bogotá",
          "Usaquén",
          "Chapinero",
          "Suba",
          "Kennedy",
          "Engativá",
          "Bosa",
          "Fontibón",
          "Teusaquillo"
        ]
      },
      {
        "name": "Antioquia",
        "code": "ANT",
        "cities": [
          "Medellín",
          "Bello",
          "Itagüí",
          "Envigado",
          "Rionegro",
          "Sabaneta",
          "Caldas",
          "Apartadó",
          "Turbo",
          "Caucasia"
        ]
      },
      {
        "name": "Valle del Cauca",
        "code": "VAC",
        "cities": [
          "Cali",
          "Palmira",
          "Buenaventura",
          "Tuluá",
          "Cartago",
          "Buga",
          "Jamundí",
          "Yumbo"
        ]
      },
      {
        "name": "Atlántico",
        "code": "ATL",
        "cities": [
          "Barranquilla",
          "Soledad",
          "Malambo",
          "Puerto Colombia",
          "Sabanalarga"
        ]
      },
      {
        "name": "Santander",
        "code": "SAN",
        "cities": [
          "Bucaramanga",
          "Floridablanca",
          "Girón",
          "Piedecuesta",
          "Barrancabermeja",
          "San Gil"
        ]
      },
      {
        "name": "Bolívar",
        "code": "BOL",
        "cities": [
          "Cartagena",
          "Magangué",
          "Turbaco",
          "Arjona",
          "El Carmen de Bolívar"
        ]
      },
      {
        "name": "Cundinamarca",
        "code": "CUN",
        "cities": [
          "Soacha",
          "Facatativá",
          "Chía",
          "Zipaquirá",
          "Mosquera",
          "Fusagasugá",
          "Madrid",
          "Funza"
        ]
      },
      {
        "name": "Norte de Santander",
        "code": "NSA",
        "cities": [
          "Cúcuta",
          "Ocaña",
          "Villa del Rosario",
          "Los Patios",
          "Pamplona"
        ]
      },
      {
        "name": "Tolima",
        "code": "TOL",
        "cities": [
          "Ibagué",
          "Espinal",
          "Melgar",
          "Chaparral",
          "Líbano"
        ]
      },
      {
        "name": "Risaralda",
        "code": "RIS",
        "cities": [
          "Pereira",
          "Dosquebradas",
          "Santa Rosa de Cabal"
        ]
      },
      {
        "name": "Caldas",
        "code": "CAL",
        "cities": [
          "Manizales",
          "La Dorada",
          "Chinchiná",
          "Villamaría"
        ]
      },
      {
        "name": "Meta",
        "code": "MET",
        "cities": [
          "Villavicencio",
          "Acacías",
          "Granada",
          "Puerto López"
        ]
      },
      {
        "name": "Huila",
        "code": "HUI",
        "cities": [
          "Neiva",
          "Pitalito",
          "Garzón",
          "La Plata"
        ]
      },
      {
        "name": "Nariño",
        "code": "NAR",
        "cities": [
          "Pasto",
          "Tumaco",
          "Ipiales",
          "Túquerres"
        ]
      },
      {
        "name": "Quindío",
        "code": "QUI",
        "cities": [
          "Armenia",
          "Calarca",
          "La Tebaida",
          "Montenegro",
          "Quimbaya"
        ]
      },
      {
        "name": "Cesar",
        "code": "CES",
        "cities": [
          "Valledupar",
          "Aguachica",
          "Agustín Codazzi"
        ]
      },
      {
        "name": "Córdoba",
        "code": "COR",
        "cities": [
          "Montería",
          "Lorica",
          "Sahagún",
          "Cereté",
          "Montelíbano"
        ]
      },
      {
        "name": "Magdalena",
        "code": "MAG",
        "cities": [
          "Santa Marta",
          "Ciénaga",
          "Fundación",
          "El Banco"
        ]
      },
      {
        "name": "Cauca",
        "code": "CAU",
        "cities": [
          "Popayán",
          "Santander de Quilichao",
          "Puerto Tejada",
          "Patía"
        ]
      },
      {
        "name": "Boyacá",
        "code": "BOY",
        "cities": [
          "Tunja",
          "Sogamoso",
          "Duitama",
          "Chiquinquirá",
          "Puerto Boyacá"
        ]
      }
    ]
  },
  {
    "name": "Comores",
    "code": "KM",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Moroni",
          "Mutsamudu",
          "Fomboni"
        ]
      }
    ]
  },
  {
    "name": "Congo (Brazzaville)",
    "code": "CG",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Brazzaville",
          "Pointe-Noire",
          "Dolisie",
          "Nkayi",
          "Ouésso"
        ]
      }
    ]
  },
  {
    "name": "Congo (RDC - Kinshasa)",
    "code": "CD",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Kinshasa",
          "Lubumbashi",
          "Mbuji-Mayi",
          "Goma",
          "Kananga",
          "Kisangani",
          "Bukavu",
          "Tshikapa",
          "Kolwezi",
          "Matadi"
        ]
      }
    ]
  },
  {
    "name": "Coreia do Norte",
    "code": "KP",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Pyongyang",
          "Hamhung",
          "Chongjin",
          "Nampo",
          "Wonsan",
          "Sinuiju"
        ]
      }
    ]
  },
  {
    "name": "Coreia do Sul",
    "code": "KR",
    "states": [
      {
        "name": "Seul",
        "code": "11",
        "cities": [
          "Seul",
          "Gangnam",
          "Hongdae (Mapo)",
          "Songpa",
          "Jung-gu",
          "Yongsan",
          "Seocho",
          "Yeongdeungpo",
          "Nowon",
          "Gwanak"
        ]
      },
      {
        "name": "Gyeonggi",
        "code": "41",
        "cities": [
          "Suwon",
          "Seongnam (Bundang/Pangyo)",
          "Goyang",
          "Yongin",
          "Bucheon",
          "Ansan",
          "Anyang",
          "Hwaseong",
          "Pyeongtaek",
          "Uijeongbu",
          "Gimpo",
          "Paju",
          "Siheung",
          "Gwangmyeong"
        ]
      },
      {
        "name": "Busan",
        "code": "26",
        "cities": [
          "Busan",
          "Haeundae",
          "Busanjin",
          "Sasang",
          "Nam-gu",
          "Saha"
        ]
      },
      {
        "name": "Incheon",
        "code": "28",
        "cities": [
          "Incheon",
          "Bupyeong",
          "Namdong",
          "Yeonsu (Songdo)",
          "Seo-gu"
        ]
      },
      {
        "name": "Daegu",
        "code": "27",
        "cities": [
          "Daegu",
          "Suseong",
          "Dalseo",
          "Buk-gu",
          "Dong-gu"
        ]
      },
      {
        "name": "Daejeon",
        "code": "30",
        "cities": [
          "Daejeon",
          "Yuseong",
          "Seo-gu",
          "Daedeok"
        ]
      },
      {
        "name": "Gwangju",
        "code": "29",
        "cities": [
          "Gwangju",
          "Buk-gu",
          "Gwangsan",
          "Seo-gu"
        ]
      },
      {
        "name": "Ulsan",
        "code": "31",
        "cities": [
          "Ulsan",
          "Nam-gu",
          "Jung-gu",
          "Ulju"
        ]
      },
      {
        "name": "Gangwon",
        "code": "42",
        "cities": [
          "Chuncheon",
          "Wonju",
          "Gangneung",
          "Sokcho"
        ]
      },
      {
        "name": "Jeju",
        "code": "49",
        "cities": [
          "Jeju",
          "Seogwipo"
        ]
      }
    ]
  },
  {
    "name": "Costa do Marfim",
    "code": "CI",
    "states": [
      {
        "name": "Abidjan",
        "code": "AB",
        "cities": [
          "Abidjan",
          "Cocody",
          "Yopougon",
          "Plateau",
          "Marcory",
          "Treichville",
          "Port-Bouët",
          "Abobo",
          "Koumassi",
          "Adjamé"
        ]
      },
      {
        "name": "Yamoussoukro",
        "code": "YM",
        "cities": [
          "Yamoussoukro"
        ]
      },
      {
        "name": "Vallée du Bandama",
        "code": "VB",
        "cities": [
          "Bouaké",
          "Katiola"
        ]
      },
      {
        "name": "Bas-Sassandra",
        "code": "BS",
        "cities": [
          "San-Pédro",
          "Sassandra",
          "Soubré"
        ]
      }
    ]
  },
  {
    "name": "Costa Rica",
    "code": "CR",
    "states": [
      {
        "name": "San José",
        "code": "SJ",
        "cities": [
          "San José",
          "Desamparados",
          "Alajuelita",
          "Pérez Zeledón",
          "Escazú",
          "Santa Ana"
        ]
      },
      {
        "name": "Alajuela",
        "code": "AL",
        "cities": [
          "Alajuela",
          "San Carlos",
          "San Ramón",
          "Grecia"
        ]
      },
      {
        "name": "Cartago",
        "code": "CA",
        "cities": [
          "Cartago",
          "La União",
          "Turrialba",
          "Paraíso"
        ]
      },
      {
        "name": "Heredia",
        "code": "HE",
        "cities": [
          "Heredia",
          "San Rafael",
          "Santo Domingo",
          "Barva"
        ]
      },
      {
        "name": "Guanacaste",
        "code": "GU",
        "cities": [
          "Liberia",
          "Nicoya",
          "Santa Cruz",
          "Cañas"
        ]
      },
      {
        "name": "Puntarenas",
        "code": "PU",
        "cities": [
          "Puntarenas",
          "Esparza",
          "Quepos",
          "Golfito",
          "Jacó"
        ]
      },
      {
        "name": "Limón",
        "code": "LI",
        "cities": [
          "Limón",
          "Pococí",
          "Siquirres",
          "Talamanca",
          "Guápiles"
        ]
      }
    ]
  },
  {
    "name": "Croácia",
    "code": "HR",
    "states": [
      {
        "name": "Cidade de Zagreb",
        "code": "21",
        "cities": [
          "Zagreb",
          "Sesvete"
        ]
      },
      {
        "name": "Split-Dalmácia",
        "code": "17",
        "cities": [
          "Split",
          "Kaštela",
          "Solin",
          "Sinj",
          "Makarska",
          "Trogir"
        ]
      },
      {
        "name": "Primorje-Gorski Kotar",
        "code": "08",
        "cities": [
          "Rijeka",
          "Opatija",
          "Crikvenica",
          "Krk"
        ]
      },
      {
        "name": "Osijek-Barânia",
        "code": "14",
        "cities": [
          "Osijek",
          "Đakovo",
          "Našice",
          "Beli Manastir"
        ]
      },
      {
        "name": "Zadar",
        "code": "13",
        "cities": [
          "Zadar",
          "Biograd na Moru",
          "Nin"
        ]
      },
      {
        "name": "Ístria",
        "code": "18",
        "cities": [
          "Pula",
          "Poreč",
          "Rovinj",
          "Umag",
          "Labin",
          "Pazin"
        ]
      },
      {
        "name": "Dubrovnik-Neretva",
        "code": "19",
        "cities": [
          "Dubrovnik",
          "Metković",
          "Ploče",
          "Korčula"
        ]
      },
      {
        "name": "Varaždin",
        "code": "05",
        "cities": [
          "Varaždin",
          "Ivanec",
          "Novi Marof"
        ]
      },
      {
        "name": "Sisak-Moslavina",
        "code": "03",
        "cities": [
          "Sisak",
          "Petrinja",
          "Kutina",
          "Novska"
        ]
      },
      {
        "name": "Karlovac",
        "code": "04",
        "cities": [
          "Karlovac",
          "Ogulin",
          "Duga Resa"
        ]
      },
      {
        "name": "Brod-Posavina",
        "code": "12",
        "cities": [
          "Slavonski Brod",
          "Nova Gradiška"
        ]
      },
      {
        "name": "Šibenik-Knin",
        "code": "15",
        "cities": [
          "Šibenik",
          "Knin",
          "Vodice"
        ]
      }
    ]
  },
  {
    "name": "Cuba",
    "code": "CU",
    "states": [
      {
        "name": "La Habana",
        "code": "03",
        "cities": [
          "Havana",
          "Playa",
          "Plaza de la Revolución",
          "Centro Habana",
          "Diez de Octubre"
        ]
      },
      {
        "name": "Santiago de Cuba",
        "code": "13",
        "cities": [
          "Santiago de Cuba",
          "Palma Soriano"
        ]
      },
      {
        "name": "Holguín",
        "code": "11",
        "cities": [
          "Holguín",
          "Moa",
          "Banes"
        ]
      },
      {
        "name": "Camagüey",
        "code": "09",
        "cities": [
          "Camagüey",
          "Florida",
          "Nuevitas"
        ]
      }
    ]
  },
  {
    "name": "Dinamarca",
    "code": "DK",
    "states": [
      {
        "name": "Hovedstaden",
        "code": "84",
        "cities": [
          "Copenhague",
          "Frederiksberg",
          "Helsingør",
          "Hillerød",
          "Gladsaxe",
          "Gentofte",
          "Lyngby-Taarbæk",
          "Taastrup",
          "Rødovre",
          "Hvidovre",
          "Albertslund"
        ]
      },
      {
        "name": "Midtjylland",
        "code": "82",
        "cities": [
          "Aarhus",
          "Randers",
          "Horsens",
          "Silkeborg",
          "Herning",
          "Viborg",
          "Holstebro",
          "Skive",
          "Ikast"
        ]
      },
      {
        "name": "Syddanmark",
        "code": "83",
        "cities": [
          "Odense",
          "Esbjerg",
          "Kolding",
          "Vejle",
          "Fredericia",
          "Sønderborg",
          "Svendborg",
          "Haderslev",
          "Aabenraa"
        ]
      },
      {
        "name": "Nordjylland",
        "code": "81",
        "cities": [
          "Aalborg",
          "Hjørring",
          "Frederikshavn",
          "Thisted",
          "Brønderslev"
        ]
      },
      {
        "name": "Sjælland",
        "code": "85",
        "cities": [
          "Roskilde",
          "Næstved",
          "Slagelse",
          "Holbæk",
          "Køge",
          "Ringsted",
          "Kalundborg",
          "Nykøbing Falster"
        ]
      }
    ]
  },
  {
    "name": "Djibuti",
    "code": "DJ",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Cidade de Djibuti",
          "Ali Sabieh",
          "Tadjoura",
          "Obock"
        ]
      }
    ]
  },
  {
    "name": "Dominica",
    "code": "DM",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Roseau",
          "Portsmouth",
          "Marigot"
        ]
      }
    ]
  },
  {
    "name": "Egito",
    "code": "EG",
    "states": [
      {
        "name": "Cairo",
        "code": "C",
        "cities": [
          "Cairo",
          "Nova Cairo",
          "Heliópolis",
          "Nasr City",
          "Maadi",
          "Zamalek",
          "Shubra"
        ]
      },
      {
        "name": "Gizé (Giza)",
        "code": "GZ",
        "cities": [
          "Gizé",
          "Cidade 6 de Outubro",
          "Sheikh Zayed",
          "Haram",
          "Dokki"
        ]
      },
      {
        "name": "Alexandria",
        "code": "ALX",
        "cities": [
          "Alexandria",
          "Borg El Arab",
          "Montaza",
          "Sidi Gaber"
        ]
      },
      {
        "name": "Dakahlia",
        "code": "DK",
        "cities": [
          "Mansoura",
          "Mit Ghamr",
          "Talkha"
        ]
      },
      {
        "name": "Mar Vermelho",
        "code": "BA",
        "cities": [
          "Hurghada",
          "El Gouna",
          "Marsa Alam",
          "Safaga"
        ]
      },
      {
        "name": "Sinai do Sul",
        "code": "JS",
        "cities": [
          "Sharm El Sheikh",
          "Dahab",
          "Nuweiba",
          "Taba"
        ]
      },
      {
        "name": "Porto Said",
        "code": "PTS",
        "cities": [
          "Porto Said",
          "Port Fouad"
        ]
      },
      {
        "name": "Suez",
        "code": "SUZ",
        "cities": [
          "Suez",
          "Ain Sokhna"
        ]
      },
      {
        "name": "Assuã",
        "code": "ASN",
        "cities": [
          "Assuã",
          "Kom Ombo",
          "Edfu"
        ]
      },
      {
        "name": "Luxor",
        "code": "LX",
        "cities": [
          "Luxor",
          "Esna",
          "Armant"
        ]
      }
    ]
  },
  {
    "name": "El Salvador",
    "code": "SV",
    "states": [
      {
        "name": "San Salvador",
        "code": "SS",
        "cities": [
          "San Salvador",
          "Soyapango",
          "Ilopango",
          "Apopa",
          "Mejicanos",
          "San Marcos"
        ]
      },
      {
        "name": "La Libertad",
        "code": "LI",
        "cities": [
          "Santa Tecla",
          "Antiguo Cuscatlán",
          "Colón",
          "San Juan Opico"
        ]
      },
      {
        "name": "Santa Ana",
        "code": "SA",
        "cities": [
          "Santa Ana",
          "Chalchuapa",
          "Metapán"
        ]
      },
      {
        "name": "San Miguel",
        "code": "SM",
        "cities": [
          "San Miguel",
          "Chinameca"
        ]
      }
    ]
  },
  {
    "name": "Emirados Árabes Unidos",
    "code": "AE",
    "states": [
      {
        "name": "Dubai",
        "code": "DU",
        "cities": [
          "Dubai",
          "Downtown Dubai",
          "Dubai Marina",
          "Jumeirah",
          "Deira",
          "Bur Dubai",
          "Business Bay",
          "JLT"
        ]
      },
      {
        "name": "Abu Dhabi",
        "code": "AZ",
        "cities": [
          "Abu Dhabi",
          "Al Ain",
          "Al Dhafra",
          "Yas Island",
          "Saadiyat Island"
        ]
      },
      {
        "name": "Sharjah",
        "code": "SH",
        "cities": [
          "Sharjah",
          "Khor Fakkan",
          "Kalba"
        ]
      },
      {
        "name": "Ajman",
        "code": "AJ",
        "cities": [
          "Ajman"
        ]
      },
      {
        "name": "Ras Al Khaimah",
        "code": "RK",
        "cities": [
          "Ras Al Khaimah"
        ]
      },
      {
        "name": "Fujairah",
        "code": "FU",
        "cities": [
          "Fujairah",
          "Dibba Al-Fujairah"
        ]
      },
      {
        "name": "Umm Al Quwain",
        "code": "UQ",
        "cities": [
          "Umm Al Quwain"
        ]
      }
    ]
  },
  {
    "name": "Equador",
    "code": "EC",
    "states": [
      {
        "name": "Pichincha",
        "code": "P",
        "cities": [
          "Quito",
          "Sangolquí",
          "Cayambe",
          "Machachi"
        ]
      },
      {
        "name": "Guayas",
        "code": "G",
        "cities": [
          "Guayaquil",
          "Durán",
          "Samborondón",
          "Milagro",
          "Daule",
          "Playas"
        ]
      },
      {
        "name": "Azuay",
        "code": "A",
        "cities": [
          "Cuenca",
          "Gualaceo",
          "Paute",
          "Santa Isabel"
        ]
      },
      {
        "name": "Manabí",
        "code": "M",
        "cities": [
          "Portoviejo",
          "Manta",
          "Chone",
          "Montecristi",
          "Bahía de Caráquez"
        ]
      },
      {
        "name": "El Oro",
        "code": "O",
        "cities": [
          "Machala",
          "Pasaje",
          "Santa Rosa",
          "Huaquillas"
        ]
      },
      {
        "name": "Tungurahua",
        "code": "T",
        "cities": [
          "Ambato",
          "Baños de Agua Santa",
          "Pelileo"
        ]
      },
      {
        "name": "Santo Domingo de los Tsáchilas",
        "code": "SD",
        "cities": [
          "Santo Domingo",
          "La Concordia"
        ]
      },
      {
        "name": "Loja",
        "code": "L",
        "cities": [
          "Loja",
          "Catamayo",
          "Cariamanga"
        ]
      },
      {
        "name": "Imbabura",
        "code": "I",
        "cities": [
          "Ibarra",
          "Otavalo",
          "Cotacachi",
          "Atuntaqui"
        ]
      },
      {
        "name": "Chimborazo",
        "code": "CH",
        "cities": [
          "Riobamba",
          "Guano",
          "Alausi"
        ]
      },
      {
        "name": "Los Ríos",
        "code": "R",
        "cities": [
          "Babahoyo",
          "Quevedo",
          "Ventanas",
          "Vinces"
        ]
      },
      {
        "name": "Esmeraldas",
        "code": "E",
        "cities": [
          "Esmeraldas",
          "Atacames",
          "Quinindé"
        ]
      },
      {
        "name": "Santa Elena",
        "code": "SE",
        "cities": [
          "Santa Elena",
          "Salinas",
          "La Libertad",
          "Montañita"
        ]
      }
    ]
  },
  {
    "name": "Eritreia",
    "code": "ER",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Asmara",
          "Keren",
          "Massawa",
          "Assab"
        ]
      }
    ]
  },
  {
    "name": "Eslováquia",
    "code": "SK",
    "states": [
      {
        "name": "Bratislava",
        "code": "BL",
        "cities": [
          "Bratislava",
          "Pezinok",
          "Senec",
          "Malacky"
        ]
      },
      {
        "name": "Košice",
        "code": "KI",
        "cities": [
          "Košice",
          "Michalovce",
          "Spišská Nová Ves",
          "Trebišov",
          "Rožňava"
        ]
      },
      {
        "name": "Prešov",
        "code": "PV",
        "cities": [
          "Prešov",
          "Poprad",
          "Humenné",
          "Bardejov",
          "Vranov nad Topľou",
          "Kežmarok"
        ]
      },
      {
        "name": "Žilina",
        "code": "ZI",
        "cities": [
          "Žilina",
          "Martin",
          "Liptovský Mikuláš",
          "Ružomberok",
          "Čadca"
        ]
      },
      {
        "name": "Nitra",
        "code": "NI",
        "cities": [
          "Nitra",
          "Nové Zámky",
          "Levice",
          "Komárno",
          "Topoľčany"
        ]
      },
      {
        "name": "Banská Bystrica",
        "code": "BC",
        "cities": [
          "Banská Bystrica",
          "Zvolen",
          "Lučenec",
          "Rimavská Sobota",
          "Brezno"
        ]
      },
      {
        "name": "Trnava",
        "code": "TA",
        "cities": [
          "Trnava",
          "Piešťany",
          "Hlohovec",
          "Dunajská Streda",
          "Galanta",
          "Senica"
        ]
      },
      {
        "name": "Trenčín",
        "code": "TC",
        "cities": [
          "Trenčín",
          "Prievidza",
          "Považská Bystrica",
          "Dubnica nad Váhom",
          "Partizánske",
          "Nové Mesto nad Váhom"
        ]
      }
    ]
  },
  {
    "name": "Eslovênia",
    "code": "SI",
    "states": [
      {
        "name": "Eslovênia",
        "code": "SI",
        "cities": [
          "Liubliana (Ljubljana)",
          "Maribor",
          "Celje",
          "Kranj",
          "Koper",
          "Velenje",
          "Novo Mesto",
          "Ptuj",
          "Trbovlje",
          "Kamnik",
          "Nova Gorica",
          "Bled"
        ]
      }
    ]
  },
  {
    "name": "Espanha",
    "code": "ES",
    "states": [
      {
        "name": "Madrid",
        "code": "MD",
        "cities": [
          "Madrid",
          "Móstoles",
          "Alcalá de Henares",
          "Fuenlabrada",
          "Leganés",
          "Getafe",
          "Alcorcón",
          "Torrejón de Ardoz",
          "Parla",
          "Alcobendas",
          "Las Rozas",
          "San Sebastián de los Reyes",
          "Pozuelo de Alarcón",
          "Rivas-Vaciamadrid",
          "Coslada",
          "Valdemoro",
          "Majadahonda",
          "Collado Villalba"
        ]
      },
      {
        "name": "Catalunha",
        "code": "CT",
        "cities": [
          "Barcelona",
          "L'Hospitalet de Llobregat",
          "Badalona",
          "Terrassa",
          "Sabadell",
          "Mataró",
          "Santa Coloma de Gramenet",
          "Sant Cugat del Vallès",
          "Cornellà de Llobregat",
          "Sant Boi de Llobregat",
          "Manresa",
          "Rubí",
          "Viladecans",
          "El Prat de Llobregat",
          "Castelldefels",
          "Granollers",
          "Cerdanyola del Vallès",
          "Tarragona",
          "Girona",
          "Lleida",
          "Reus"
        ]
      },
      {
        "name": "Andaluzia",
        "code": "AN",
        "cities": [
          "Sevilha",
          "Málaga",
          "Córdova",
          "Granada",
          "Jerez de la Frontera",
          "Almería",
          "Huelva",
          "Marbella",
          "Dos Hermanas",
          "Algeciras",
          "Cádis",
          "Jaén",
          "Roquetas de Mar",
          "San Fernando",
          "El Puerto de Santa María",
          "Mijas",
          "Chiclana de la Frontera",
          "Fuengirola",
          "Vélez-Málaga",
          "Alcalá de Guadaíra",
          "Torremolinos",
          "Benalmádena",
          "Sanlúcar de Barrameda",
          "Estepona",
          "La Línea de la Concepción",
          "Motril"
        ]
      },
      {
        "name": "Comunidade Valenciana",
        "code": "VC",
        "cities": [
          "Valência",
          "Alicante",
          "Elche",
          "Castellón de la Plana",
          "Torrevieja",
          "Torrent",
          "Orihuela",
          "Gandia",
          "Paterna",
          "Benidorm",
          "Sagunto",
          "Alcoy",
          "San Vicente del Raspeig",
          "Elda",
          "Villarreal"
        ]
      },
      {
        "name": "Galiza",
        "code": "GA",
        "cities": [
          "Vigo",
          "A Coruña",
          "Ourense",
          "Lugo",
          "Santiago de Compostela",
          "Pontevedra",
          "Ferrol"
        ]
      },
      {
        "name": "País Basco",
        "code": "PV",
        "cities": [
          "Bilbau",
          "Vitoria-Gasteiz",
          "San Sebastián (Donostia)",
          "Barakaldo",
          "Getxo",
          "Irun",
          "Portugalete",
          "Santurtzi"
        ]
      },
      {
        "name": "Castela e Leão",
        "code": "CL",
        "cities": [
          "Valladolid",
          "Burgos",
          "Salamanca",
          "Leão",
          "Palência",
          "Ponferrada",
          "Zamora",
          "Ávila",
          "Segóvia",
          "Sória"
        ]
      },
      {
        "name": "Canárias",
        "code": "CN",
        "cities": [
          "Las Palmas de Gran Canaria",
          "Santa Cruz de Tenerife",
          "San Cristóbal de La Laguna",
          "Telde",
          "Arona",
          "Santa Lucía de Tirajana",
          "Arrecife"
        ]
      },
      {
        "name": "Castela-Mancha",
        "code": "CM",
        "cities": [
          "Albacete",
          "Talavera de la Reina",
          "Toledo",
          "Guadalajara",
          "Ciudad Real",
          "Cuenca",
          "Puertollano"
        ]
      },
      {
        "name": "Região de Múrcia",
        "code": "MC",
        "cities": [
          "Múrcia",
          "Cartagena",
          "Lorca",
          "Molina de Segura",
          "Alcantarilla"
        ]
      },
      {
        "name": "Aragão",
        "code": "AR",
        "cities": [
          "Saragoça",
          "Huesca",
          "Teruel"
        ]
      },
      {
        "name": "Ilhas Baleares",
        "code": "IB",
        "cities": [
          "Palma de Maiorca",
          "Calvià",
          "Ibiza",
          "Manacor",
          "Santa Eulària des Riu",
          "Ciutadella de Menorca",
          "Maó-Mahón"
        ]
      },
      {
        "name": "Extremadura",
        "code": "EX",
        "cities": [
          "Badajoz",
          "Cáceres",
          "Mérida",
          "Plasencia",
          "Don Benito"
        ]
      },
      {
        "name": "Astúrias",
        "code": "AS",
        "cities": [
          "Gijón",
          "Oviedo",
          "Avilés",
          "Siero"
        ]
      },
      {
        "name": "Navarra",
        "code": "NC",
        "cities": [
          "Pamplona",
          "Tudela",
          "Barañáin"
        ]
      },
      {
        "name": "Cantábria",
        "code": "CB",
        "cities": [
          "Santander",
          "Torrelavega",
          "Castro-Urdiales"
        ]
      },
      {
        "name": "La Rioja",
        "code": "RI",
        "cities": [
          "Logronho",
          "Calahorra",
          "Arnedo"
        ]
      }
    ]
  },
  {
    "name": "Essuatíni",
    "code": "SZ",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Mbabane",
          "Manzini",
          "Big Bend",
          "Mhlume"
        ]
      }
    ]
  },
  {
    "name": "Estados Unidos",
    "code": "US",
    "states": [
      {
        "name": "Califórnia",
        "code": "CA",
        "cities": [
          "Los Angeles",
          "San Francisco",
          "San Diego",
          "San Jose",
          "Sacramento",
          "Fresno",
          "Long Beach",
          "Oakland",
          "Anaheim",
          "Irvine",
          "Bakersfield",
          "Riverside",
          "Santa Ana",
          "Chula Vista",
          "Fremont",
          "San Bernardino",
          "Modesto",
          "Fontana",
          "Oxnard",
          "Moreno Valley",
          "Huntington Beach",
          "Glendale",
          "Santa Clarita",
          "Garden Grove",
          "Oceanside",
          "Rancho Cucamonga",
          "Ontario",
          "Lancaster",
          "Elk Grove",
          "Palmdale",
          "Corona",
          "Salinas",
          "Pomona",
          "Torrance",
          "Hayward",
          "Escondido",
          "Sunnyvale",
          "Pasadena",
          "Orange",
          "Fullerton"
        ]
      },
      {
        "name": "Texas",
        "code": "TX",
        "cities": [
          "Houston",
          "Dallas",
          "Austin",
          "San Antonio",
          "Fort Worth",
          "El Paso",
          "Arlington",
          "Corpus Christi",
          "Plano",
          "Laredo",
          "Lubbock",
          "Garland",
          "Irving",
          "Amarillo",
          "Grand Prairie",
          "Brownsville",
          "McKinney",
          "Frisco",
          "Pasadena",
          "Mesquite",
          "Killeen",
          "McAllen",
          "Carrollton",
          "Midland",
          "Waco",
          "Denton",
          "Abilene",
          "Odessa",
          "Beaumont",
          "Round Rock",
          "The Woodlands",
          "Richardson",
          "Pearland",
          "College Station",
          "Wichita Falls",
          "Lewisville",
          "Tyler",
          "San Angelo",
          "League City",
          "Allen"
        ]
      },
      {
        "name": "Flórida",
        "code": "FL",
        "cities": [
          "Miami",
          "Orlando",
          "Tampa",
          "Jacksonville",
          "Fort Lauderdale",
          "St. Petersburg",
          "Hialeah",
          "Tallahassee",
          "Cape Coral",
          "Pembroke Pines",
          "Hollywood",
          "Gainesville",
          "Miramar",
          "Coral Springs",
          "Clearwater",
          "Palm Bay",
          "Pompano Beach",
          "West Palm Beach",
          "Lakeland",
          "Davie",
          "Miami Gardens",
          "Boca Raton",
          "Sunrise",
          "Plantation",
          "Delray Beach",
          "Fort Myers",
          "Kissimmee",
          "Daytona Beach",
          "Sarasota",
          "Bradenton",
          "Naples",
          "Key West",
          "Pensacola"
        ]
      },
      {
        "name": "Nova York",
        "code": "NY",
        "cities": [
          "New York City",
          "Buffalo",
          "Rochester",
          "Yonkers",
          "Syracuse",
          "Albany",
          "New Rochelle",
          "Mount Vernon",
          "Schenectady",
          "Utica",
          "White Plains",
          "Hempstead",
          "Troy",
          "Niagara Falls",
          "Binghamton",
          "Freeport",
          "Valley Stream",
          "Long Beach",
          "Ithaca"
        ]
      },
      {
        "name": "Washington",
        "code": "WA",
        "cities": [
          "Seattle",
          "Spokane",
          "Tacoma",
          "Vancouver",
          "Bellevue",
          "Kent",
          "Everett",
          "Renton",
          "Spokane Valley",
          "Federal Way",
          "Yakima",
          "Bellingham",
          "Kirkland",
          "Kennewick",
          "Auburn",
          "Pasco",
          "Marysville",
          "Redmond",
          "Lakewood",
          "Olympia"
        ]
      },
      {
        "name": "Illinois",
        "code": "IL",
        "cities": [
          "Chicago",
          "Aurora",
          "Naperville",
          "Joliet",
          "Rockford",
          "Springfield",
          "Elgin",
          "Peoria",
          "Champaign",
          "Waukegan",
          "Cicero",
          "Bloomington",
          "Arlington Heights",
          "Evanston",
          "Decatur",
          "Schaumburg",
          "Bolingbrook",
          "Palatine",
          "Skokie",
          "Des Plaines"
        ]
      },
      {
        "name": "Massachusetts",
        "code": "MA",
        "cities": [
          "Boston",
          "Worcester",
          "Springfield",
          "Cambridge",
          "Lowell",
          "Brockton",
          "Quincy",
          "Lynn",
          "New Bedford",
          "Fall River",
          "Newton",
          "Lawrence",
          "Somerville",
          "Framingham",
          "Haverhill",
          "Malden",
          "Waltham",
          "Brookline",
          "Plymouth",
          "Medford"
        ]
      },
      {
        "name": "Pensilvânia",
        "code": "PA",
        "cities": [
          "Filadélfia",
          "Pittsburgh",
          "Allentown",
          "Reading",
          "Erie",
          "Upper Darby",
          "Scranton",
          "Bethlehem",
          "Lancaster",
          "Harrisburg",
          "Altoona",
          "York",
          "State College",
          "Wilkes-Barre"
        ]
      },
      {
        "name": "Ohio",
        "code": "OH",
        "cities": [
          "Columbus",
          "Cleveland",
          "Cincinnati",
          "Toledo",
          "Akron",
          "Dayton",
          "Parma",
          "Canton",
          "Youngstown",
          "Lorain",
          "Hamilton",
          "Springfield",
          "Kettering",
          "Elyria",
          "Lakewood"
        ]
      },
      {
        "name": "Geórgia",
        "code": "GA",
        "cities": [
          "Atlanta",
          "Augusta",
          "Columbus",
          "Macon",
          "Savannah",
          "Athens",
          "Sandy Springs",
          "Roswell",
          "Johns Creek",
          "Albany",
          "Warner Robins",
          "Alpharetta",
          "Marietta",
          "Valdosta"
        ]
      },
      {
        "name": "Carolina do Norte",
        "code": "NC",
        "cities": [
          "Charlotte",
          "Raleigh",
          "Greensboro",
          "Durham",
          "Winston-Salem",
          "Fayetteville",
          "Cary",
          "Wilmington",
          "High Point",
          "Concord",
          "Asheville",
          "Greenville",
          "Gastonia",
          "Apex"
        ]
      },
      {
        "name": "Nova Jersey",
        "code": "NJ",
        "cities": [
          "Newark",
          "Jersey City",
          "Paterson",
          "Elizabeth",
          "Edison",
          "Woodbridge",
          "Lakewood",
          "Toms River",
          "Hamilton",
          "Trenton",
          "Clifton",
          "Camden",
          "Brick",
          "Cherry Hill",
          "Passaic",
          "Hoboken",
          "Atlantic City"
        ]
      },
      {
        "name": "Virgínia",
        "code": "VA",
        "cities": [
          "Virginia Beach",
          "Norfolk",
          "Chesapeake",
          "Richmond",
          "Newport News",
          "Alexandria",
          "Hampton",
          "Roanoke",
          "Portsmouth",
          "Suffolk",
          "Lynchburg",
          "Arlington",
          "Fairfax",
          "Charlottesville"
        ]
      },
      {
        "name": "Colorado",
        "code": "CO",
        "cities": [
          "Denver",
          "Colorado Springs",
          "Aurora",
          "Fort Collins",
          "Lakewood",
          "Thornton",
          "Arvada",
          "Westminster",
          "Pueblo",
          "Centennial",
          "Boulder",
          "Greeley",
          "Longmont",
          "Loveland",
          "Aspen"
        ]
      },
      {
        "name": "Arizona",
        "code": "AZ",
        "cities": [
          "Phoenix",
          "Tucson",
          "Mesa",
          "Chandler",
          "Scottsdale",
          "Glendale",
          "Gilbert",
          "Tempe",
          "Peoria",
          "Surprise",
          "Yuma",
          "Avondale",
          "Flagstaff",
          "Goodyear",
          "Lake Havasu City"
        ]
      },
      {
        "name": "Nevada",
        "code": "NV",
        "cities": [
          "Las Vegas",
          "Henderson",
          "Reno",
          "North Las Vegas",
          "Sparks",
          "Carson City",
          "Elko",
          "Mesquite",
          "Boulder City"
        ]
      },
      {
        "name": "Michigan",
        "code": "MI",
        "cities": [
          "Detroit",
          "Grand Rapids",
          "Warren",
          "Sterling Heights",
          "Ann Arbor",
          "Lansing",
          "Flint",
          "Dearborn",
          "Livonia",
          "Troy",
          "Kalamazoo"
        ]
      },
      {
        "name": "Tennessee",
        "code": "TN",
        "cities": [
          "Nashville",
          "Memphis",
          "Knoxville",
          "Chattanooga",
          "Clarksville",
          "Murfreesboro",
          "Franklin",
          "Johnson City",
          "Jackson"
        ]
      },
      {
        "name": "Indiana",
        "code": "IN",
        "cities": [
          "Indianápolis",
          "Fort Wayne",
          "Evansville",
          "South Bend",
          "Carmel",
          "Fishers",
          "Bloomington",
          "Hammond",
          "Gary",
          "Lafayette"
        ]
      },
      {
        "name": "Missouri",
        "code": "MO",
        "cities": [
          "Kansas City",
          "St. Louis",
          "Springfield",
          "Columbia",
          "Independence",
          "Lee's Summit",
          "O'Fallon",
          "St. Joseph"
        ]
      },
      {
        "name": "Maryland",
        "code": "MD",
        "cities": [
          "Baltimore",
          "Frederick",
          "Rockville",
          "Gaithersburg",
          "Bowie",
          "Hagerstown",
          "Annapolis",
          "Bethesda",
          "Silver Spring"
        ]
      },
      {
        "name": "Wisconsin",
        "code": "WI",
        "cities": [
          "Milwaukee",
          "Madison",
          "Green Bay",
          "Kenosha",
          "Racine",
          "Appleton",
          "Waukesha",
          "Oshkosh",
          "Eau Claire"
        ]
      },
      {
        "name": "Minnesota",
        "code": "MN",
        "cities": [
          "Minneapolis",
          "Saint Paul",
          "Rochester",
          "Bloomington",
          "Duluth",
          "Brooklyn Park",
          "Plymouth",
          "Woodbury"
        ]
      },
      {
        "name": "Carolina do Sul",
        "code": "SC",
        "cities": [
          "Charleston",
          "Columbia",
          "North Charleston",
          "Mount Pleasant",
          "Rock Hill",
          "Greenville",
          "Summerville",
          "Myrtle Beach"
        ]
      },
      {
        "name": "Alabama",
        "code": "AL",
        "cities": [
          "Huntsville",
          "Birmingham",
          "Montgomery",
          "Mobile",
          "Tuscaloosa",
          "Hoover",
          "Auburn",
          "Dothan"
        ]
      },
      {
        "name": "Louisiana",
        "code": "LA",
        "cities": [
          "Nova Orleans",
          "Baton Rouge",
          "Shreveport",
          "Lafayette",
          "Lake Charles",
          "Kenner",
          "Bossier City",
          "Monroe"
        ]
      },
      {
        "name": "Kentucky",
        "code": "KY",
        "cities": [
          "Louisville",
          "Lexington",
          "Bowling Green",
          "Owensboro",
          "Covington",
          "Richmond",
          "Georgetown"
        ]
      },
      {
        "name": "Oregon",
        "code": "OR",
        "cities": [
          "Portland",
          "Eugene",
          "Salem",
          "Gresham",
          "Hillsboro",
          "Beaverton",
          "Bend",
          "Medford",
          "Springfield",
          "Corvallis"
        ]
      },
      {
        "name": "Oklahoma",
        "code": "OK",
        "cities": [
          "Oklahoma City",
          "Tulsa",
          "Norman",
          "Broken Arrow",
          "Edmond",
          "Lawton",
          "Moore",
          "Midwest City"
        ]
      },
      {
        "name": "Connecticut",
        "code": "CT",
        "cities": [
          "Bridgeport",
          "Stamford",
          "New Haven",
          "Hartford",
          "Waterbury",
          "Norwalk",
          "Danbury",
          "New Britain"
        ]
      },
      {
        "name": "Utah",
        "code": "UT",
        "cities": [
          "Salt Lake City",
          "West Valley City",
          "Provo",
          "West Jordan",
          "Orem",
          "Sandy",
          "Ogden",
          "St. George"
        ]
      },
      {
        "name": "Iowa",
        "code": "IA",
        "cities": [
          "Des Moines",
          "Cedar Rapids",
          "Davenport",
          "Sioux City",
          "Iowa City",
          "Waterloo",
          "Ames"
        ]
      },
      {
        "name": "Kansas",
        "code": "KS",
        "cities": [
          "Wichita",
          "Overland Park",
          "Kansas City",
          "Olathe",
          "Topeka",
          "Lawrence",
          "Shawnee",
          "Manhattan"
        ]
      },
      {
        "name": "Arkansas",
        "code": "AR",
        "cities": [
          "Little Rock",
          "Fayetteville",
          "Fort Smith",
          "Springdale",
          "Jonesboro",
          "Rogers",
          "Conway"
        ]
      },
      {
        "name": "Mississippi",
        "code": "MS",
        "cities": [
          "Jackson",
          "Gulfport",
          "Southaven",
          "Hattiesburg",
          "Biloxi",
          "Tupelo",
          "Meridian"
        ]
      },
      {
        "name": "Havaí",
        "code": "HI",
        "cities": [
          "Honolulu",
          "East Honolulu",
          "Pearl City",
          "Hilo",
          "Kailua",
          "Waipahu",
          "Kaneohe",
          "Kahului"
        ]
      },
      {
        "name": "Alasca",
        "code": "AK",
        "cities": [
          "Anchorage",
          "Fairbanks",
          "Juneau",
          "Sitka",
          "Ketchikan",
          "Wasilla"
        ]
      },
      {
        "name": "Distrito de Columbia",
        "code": "DC",
        "cities": [
          "Washington D.C."
        ]
      }
    ]
  },
  {
    "name": "Estônia",
    "code": "EE",
    "states": [
      {
        "name": "Harju",
        "code": "37",
        "cities": [
          "Tallinn",
          "Maardu",
          "Keila",
          "Viimsi",
          "Saue"
        ]
      },
      {
        "name": "Tartu",
        "code": "78",
        "cities": [
          "Tartu",
          "Elva"
        ]
      },
      {
        "name": "Ida-Viru",
        "code": "44",
        "cities": [
          "Narva",
          "Kohtla-Järve",
          "Sillamäe",
          "Jõhvi"
        ]
      },
      {
        "name": "Pärnu",
        "code": "67",
        "cities": [
          "Pärnu",
          "Sindi"
        ]
      }
    ]
  },
  {
    "name": "Etiópia",
    "code": "ET",
    "states": [
      {
        "name": "Adis Abeba",
        "code": "AA",
        "cities": [
          "Adis Abeba",
          "Bole",
          "Kirkos",
          "Arada",
          "Yeka"
        ]
      },
      {
        "name": "Oromia",
        "code": "OR",
        "cities": [
          "Adama (Nazret)",
          "Bishoftu (Debre Zeyit)",
          "Jimma",
          "Shashamane"
        ]
      },
      {
        "name": "Amhara",
        "code": "AM",
        "cities": [
          "Bahir Dar",
          "Gondar",
          "Dessie"
        ]
      },
      {
        "name": "Tigray",
        "code": "TI",
        "cities": [
          "Mekele",
          "Adigrat",
          "Shire"
        ]
      },
      {
        "name": "Dire Dawa",
        "code": "DD",
        "cities": [
          "Dire Dawa"
        ]
      },
      {
        "name": "Sidama",
        "code": "SI",
        "cities": [
          "Hawassa"
        ]
      }
    ]
  },
  {
    "name": "Fiji",
    "code": "FJ",
    "states": [
      {
        "name": "Fiji",
        "code": "FJ",
        "cities": [
          "Suva",
          "Lautoka",
          "Nadi",
          "Labasa",
          "Nausori",
          "Ba"
        ]
      }
    ]
  },
  {
    "name": "Filipinas",
    "code": "PH",
    "states": [
      {
        "name": "Metro Manila (NCR)",
        "code": "00",
        "cities": [
          "Manila",
          "Quezon City",
          "Makati",
          "Taguig (BGC)",
          "Pasig",
          "Parañaque",
          "Caloocan",
          "Las Piñas",
          "Mandaluyong",
          "Marikina",
          "Muntinlupa",
          "Pasay",
          "Valenzuela",
          "San Juan"
        ]
      },
      {
        "name": "Calabarzon",
        "code": "40",
        "cities": [
          "Antipolo",
          "Dasmariñas",
          "Bacoor",
          "Calamba",
          "Santa Rosa",
          "Imus",
          "Batangas City",
          "Lipa",
          "Lucena",
          "San Pedro"
        ]
      },
      {
        "name": "Visayas Central",
        "code": "07",
        "cities": [
          "Cebu City",
          "Mandaue",
          "Lapu-Lapu",
          "Talisay",
          "Dumaguete",
          "Tagbilaran"
        ]
      },
      {
        "name": "Davao (Região XI)",
        "code": "11",
        "cities": [
          "Davao City",
          "Tagum",
          "Panabo",
          "Digos"
        ]
      },
      {
        "name": "Luzon Central",
        "code": "03",
        "cities": [
          "San Fernando",
          "Angeles",
          "Olongapo",
          "Tarlac City",
          "Malolos",
          "San Jose del Monte",
          "Cabanatuan",
          "Mabalacat"
        ]
      },
      {
        "name": "Iloilo (Visayas Ocidental)",
        "code": "06",
        "cities": [
          "Iloilo City",
          "Bacolod",
          "Roxas City"
        ]
      },
      {
        "name": "Norte de Mindanao",
        "code": "10",
        "cities": [
          "Cagayan de Oro",
          "Iligan",
          "Valencia",
          "Malaybalay"
        ]
      }
    ]
  },
  {
    "name": "Finlândia",
    "code": "FI",
    "states": [
      {
        "name": "Uusimaa",
        "code": "18",
        "cities": [
          "Helsinque",
          "Espoo",
          "Vantaa",
          "Porvoo",
          "Hyvinkää",
          "Järvenpää",
          "Nurmijärvi",
          "Kirkkonummi",
          "Tuusula",
          "Lohja",
          "Kerava"
        ]
      },
      {
        "name": "Pirkanmaa",
        "code": "11",
        "cities": [
          "Tampere",
          "Nokia",
          "Ylöjärvi",
          "Kangasala",
          "Sastamala",
          "Lempäälä",
          "Valkeakoski"
        ]
      },
      {
        "name": "Finlândia Própria (Varsinais-Suomi)",
        "code": "19",
        "cities": [
          "Turku",
          "Kaarina",
          "Salo",
          "Raisio",
          "Naantali",
          "Lieto"
        ]
      },
      {
        "name": "Ostrobótnia do Norte",
        "code": "14",
        "cities": [
          "Oulu",
          "Raahe",
          "Kuusamo",
          "Ylivieska",
          "Kempele"
        ]
      },
      {
        "name": "Finlândia Central",
        "code": "08",
        "cities": [
          "Jyväskylä",
          "Äänekoski",
          "Jämsä",
          "Laukaa"
        ]
      },
      {
        "name": "Savônia do Norte",
        "code": "15",
        "cities": [
          "Kuopio",
          "Iisalmi",
          "Varkaus",
          "Siilinjärvi"
        ]
      },
      {
        "name": "Päijät-Häme",
        "code": "16",
        "cities": [
          "Lahti",
          "Hollola",
          "Heinola",
          "Orimattila"
        ]
      },
      {
        "name": "Satakunta",
        "code": "17",
        "cities": [
          "Pori",
          "Rauma",
          "Ulvila",
          "Kankaanpää"
        ]
      },
      {
        "name": "Lapônia",
        "code": "10",
        "cities": [
          "Rovaniemi",
          "Tornio",
          "Kemi",
          "Sodankylä",
          "Inari"
        ]
      }
    ]
  },
  {
    "name": "França",
    "code": "FR",
    "states": [
      {
        "name": "Île-de-France",
        "code": "IDF",
        "cities": [
          "Paris",
          "Boulogne-Billancourt",
          "Saint-Denis",
          "Argenteuil",
          "Montreuil",
          "Nanterre",
          "Créteil",
          "Versalhes",
          "Courbevoie",
          "Vitry-sur-Seine",
          "Colombes",
          "Asnières-sur-Seine",
          "Aulnay-sous-Bois",
          "Rueil-Malmaison",
          "Aubervilliers",
          "Champigny-sur-Marne",
          "Saint-Maur-des-Fossés",
          "Drancy",
          "Issy-les-Moulineaux",
          "Levallois-Perret",
          "Noisy-le-Grand",
          "Neuilly-sur-Seine",
          "Antony",
          "Cergy",
          "Clichy",
          "Ivry-sur-Seine",
          "Sarcelles",
          "Villejuif"
        ]
      },
      {
        "name": "Auvergne-Rhône-Alpes",
        "code": "ARA",
        "cities": [
          "Lyon",
          "Saint-Étienne",
          "Grenoble",
          "Villeurbanne",
          "Clermont-Ferrand",
          "Annecy",
          "Vénissieux",
          "Chambéry",
          "Valence",
          "Vaulx-en-Velin",
          "Saint-Priest",
          "Caluire-et-Cuire",
          "Bourg-en-Bresse",
          "Bron"
        ]
      },
      {
        "name": "Provença-Alpes-Costa Azul",
        "code": "PACA",
        "cities": [
          "Marselha",
          "Nice",
          "Toulon",
          "Aix-en-Provence",
          "Avignon",
          "Cannes",
          "Antibes",
          "La Seyne-sur-Mer",
          "Hyères",
          "Arles",
          "Fréjus",
          "Grasse",
          "Cagnes-sur-Mer"
        ]
      },
      {
        "name": "Nova Aquitânia",
        "code": "NAQ",
        "cities": [
          "Bordéus",
          "Limoges",
          "Poitiers",
          "Pau",
          "La Rochelle",
          "Mérignac",
          "Pessac",
          "Bayonne",
          "Angoulême",
          "Talence",
          "Agen",
          "Brive-la-Gaillarde",
          "Biarritz"
        ]
      },
      {
        "name": "Occitânia",
        "code": "OCC",
        "cities": [
          "Toulouse",
          "Montpellier",
          "Nîmes",
          "Perpignan",
          "Béziers",
          "Montauban",
          "Narbonne",
          "Albi",
          "Carcassonne",
          "Sète",
          "Tarbes",
          "Castres",
          "Colomiers"
        ]
      },
      {
        "name": "Altos da França",
        "code": "HDF",
        "cities": [
          "Lille",
          "Amiens",
          "Roubaix",
          "Tourcoing",
          "Dunkerque",
          "Calais",
          "Villeneuve-d'Ascq",
          "Beauvais",
          "Saint-Quentin",
          "Valenciennes",
          "Boulogne-sur-Mer",
          "Douai"
        ]
      },
      {
        "name": "Grande Leste",
        "code": "GES",
        "cities": [
          "Estrasburgo",
          "Reims",
          "Metz",
          "Mulhouse",
          "Nancy",
          "Colmar",
          "Troyes",
          "Charleville-Mézières",
          "Thionville",
          "Épinal"
        ]
      },
      {
        "name": "País do Loire",
        "code": "PDL",
        "cities": [
          "Nantes",
          "Angers",
          "Le Mans",
          "Saint-Nazaire",
          "Cholet",
          "La Roche-sur-Yon",
          "Laval",
          "Saint-Herblain"
        ]
      },
      {
        "name": "Bretanha",
        "code": "BRE",
        "cities": [
          "Rennes",
          "Brest",
          "Quimper",
          "Lorient",
          "Vannes",
          "Saint-Malo",
          "Saint-Brieuc"
        ]
      },
      {
        "name": "Normandia",
        "code": "NOR",
        "cities": [
          "Le Havre",
          "Rouen",
          "Caen",
          "Cherbourg-en-Cotentin",
          "Évreux"
        ]
      },
      {
        "name": "Borgonha-Franco-Condado",
        "code": "BFC",
        "cities": [
          "Dijon",
          "Besançon",
          "Belfort",
          "Chalon-sur-Saône",
          "Nevers",
          "Auxerre",
          "Mâcon"
        ]
      },
      {
        "name": "Centro-Vale do Loire",
        "code": "CVL",
        "cities": [
          "Tours",
          "Orléans",
          "Bourges",
          "Blois",
          "Châteauroux",
          "Chartres"
        ]
      },
      {
        "name": "Córsega",
        "code": "COR",
        "cities": [
          "Ajaccio",
          "Bastia",
          "Porto-Vecchio",
          "Corte"
        ]
      }
    ]
  },
  {
    "name": "Gabão",
    "code": "GA",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Libreville",
          "Port-Gentil",
          "Franceville",
          "Oyem",
          "Moanda"
        ]
      }
    ]
  },
  {
    "name": "Gâmbia",
    "code": "GM",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Banjul",
          "Serekunda",
          "Brikama",
          "Bakau"
        ]
      }
    ]
  },
  {
    "name": "Gana",
    "code": "GH",
    "states": [
      {
        "name": "Greater Accra",
        "code": "AA",
        "cities": [
          "Accra",
          "Tema",
          "Madina",
          "Adenta",
          "Ashaiman",
          "Teshie",
          "Kasoa"
        ]
      },
      {
        "name": "Ashanti",
        "code": "AH",
        "cities": [
          "Kumasi",
          "Obuasi",
          "Tafo",
          "Ejisu"
        ]
      },
      {
        "name": "Western",
        "code": "WP",
        "cities": [
          "Sekondi-Takoradi",
          "Tarkwa"
        ]
      },
      {
        "name": "Central",
        "code": "CP",
        "cities": [
          "Cape Coast",
          "Winneba"
        ]
      }
    ]
  },
  {
    "name": "Geórgia",
    "code": "GE",
    "states": [
      {
        "name": "Tbilisi",
        "code": "TB",
        "cities": [
          "Tbilisi",
          "Batumi",
          "Kutaisi",
          "Rustavi",
          "Gori",
          "Zugdidi",
          "Poti"
        ]
      }
    ]
  },
  {
    "name": "Granada",
    "code": "GD",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "St. George's",
          "Gouyave",
          "Grenville",
          "Victoria"
        ]
      }
    ]
  },
  {
    "name": "Grécia",
    "code": "GR",
    "states": [
      {
        "name": "Ática",
        "code": "I",
        "cities": [
          "Atenas",
          "Pireu",
          "Peristeri",
          "Kallithea",
          "Acharnes",
          "Glyfada",
          "Ilio",
          "Marousi",
          "Kifisia",
          "Nea Smyrni",
          "Chalandri"
        ]
      },
      {
        "name": "Macedônia Central",
        "code": "B",
        "cities": [
          "Salônica (Thessaloniki)",
          "Kalamaria",
          "Katerini",
          "Serres",
          "Giannitsa",
          "Veria",
          "Kilkis"
        ]
      },
      {
        "name": "Grécia Ocidental",
        "code": "G",
        "cities": [
          "Patras",
          "Agrinio",
          "Aigio",
          "Messolonghi",
          "Pyrgos"
        ]
      },
      {
        "name": "Creta",
        "code": "M",
        "cities": [
          "Heraclião",
          "Chania",
          "Rethymno",
          "Agios Nikolaos",
          "Ierapetra"
        ]
      },
      {
        "name": "Tessália",
        "code": "E",
        "cities": [
          "Larissa",
          "Volos",
          "Trikala",
          "Karditsa"
        ]
      },
      {
        "name": "Peloponeso",
        "code": "J",
        "cities": [
          "Calamata",
          "Corinto",
          "Trípoli",
          "Argos",
          "Esparta"
        ]
      },
      {
        "name": "Epiro",
        "code": "D",
        "cities": [
          "Janina (Ioannina)",
          "Arta",
          "Preveza",
          "Igoumenitsa"
        ]
      },
      {
        "name": "Macedônia Oriental e Trácia",
        "code": "A",
        "cities": [
          "Alexandrópolis",
          "Kavala",
          "Xanthi",
          "Komotini",
          "Drama"
        ]
      },
      {
        "name": "Egeu Meridional",
        "code": "L",
        "cities": [
          "Rodes",
          "Cós",
          "Mykonos",
          "Santorini (Thira)",
          "Syros"
        ]
      },
      {
        "name": "Ilhas Jônicas",
        "code": "F",
        "cities": [
          "Corfu (Kérkyra)",
          "Cefalônia",
          "Zacinto (Zante)",
          "Lêucade"
        ]
      }
    ]
  },
  {
    "name": "Guam",
    "code": "GU",
    "states": [
      {
        "name": "Guam",
        "code": "GU",
        "cities": [
          "Hagåtña",
          "Dededo",
          "Yigo",
          "Tamuning",
          "Mangilao"
        ]
      }
    ]
  },
  {
    "name": "Guatemala",
    "code": "GT",
    "states": [
      {
        "name": "Guatemala",
        "code": "GU",
        "cities": [
          "Cidade da Guatemala",
          "Mixco",
          "Villa Nueva",
          "Petapa",
          "San Juan Sacatepéquez"
        ]
      },
      {
        "name": "Quetzaltenango",
        "code": "QZ",
        "cities": [
          "Quetzaltenango (Xela)",
          "Coatepeque",
          "Salcajá"
        ]
      },
      {
        "name": "Escuintla",
        "code": "ES",
        "cities": [
          "Escuintla",
          "Santa Lucía Cotzumalguapa",
          "Puerto San José"
        ]
      }
    ]
  },
  {
    "name": "Guiana",
    "code": "GY",
    "states": [
      {
        "name": "Demerara-Mahaica",
        "code": "04",
        "cities": [
          "Georgetown",
          "Linden",
          "New Amsterdam",
          "Bartica",
          "Anna Regina"
        ]
      }
    ]
  },
  {
    "name": "Guiana Francesa",
    "code": "GF",
    "states": [
      {
        "name": "Guiana Francesa",
        "code": "GF",
        "cities": [
          "Caiena",
          "Saint-Laurent-du-Maroni",
          "Kourou",
          "Matoury",
          "Remire-Montjoly"
        ]
      }
    ]
  },
  {
    "name": "Guiné",
    "code": "GN",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Conacri",
          "Nzérékoré",
          "Kankan",
          "Kindia",
          "Labé"
        ]
      }
    ]
  },
  {
    "name": "Guiné Equatorial",
    "code": "GQ",
    "states": [
      {
        "name": "Bioko / Litoral",
        "code": "GQ",
        "cities": [
          "Malabo",
          "Bata",
          "Oyala (Ciudad de la Paz)",
          "Ebebiyín",
          "Mongomo",
          "Luba",
          "Evinayong"
        ]
      }
    ]
  },
  {
    "name": "Guiné-Bissau",
    "code": "GW",
    "states": [
      {
        "name": "Bissau",
        "code": "BS",
        "cities": [
          "Bissau",
          "Bafatá",
          "Gabú",
          "Bissorã",
          "Bolama",
          "Cacheu",
          "Canchungo",
          "Catió",
          "Farim",
          "Mansôa",
          "Quinhámel"
        ]
      }
    ]
  },
  {
    "name": "Haiti",
    "code": "HT",
    "states": [
      {
        "name": "Ouest",
        "code": "OU",
        "cities": [
          "Porto Príncipe",
          "Carrefour",
          "Delmas",
          "Pétion-Ville",
          "Cité Soleil",
          "Cap-Haïtien",
          "Gonaïves"
        ]
      }
    ]
  },
  {
    "name": "Honduras",
    "code": "HN",
    "states": [
      {
        "name": "Francisco Morazán",
        "code": "FM",
        "cities": [
          "Tegucigalpa",
          "Comayagüela"
        ]
      },
      {
        "name": "Cortés",
        "code": "CR",
        "cities": [
          "San Pedro Sula",
          "Choloma",
          "Puerto Cortés",
          "Villanueva"
        ]
      },
      {
        "name": "Atlántida",
        "code": "AT",
        "cities": [
          "La Ceiba",
          "Tela"
        ]
      }
    ]
  },
  {
    "name": "Hong Kong",
    "code": "HK",
    "states": [
      {
        "name": "Hong Kong",
        "code": "HK",
        "cities": [
          "Hong Kong Island (Central/Wan Chai/Causeway Bay)",
          "Kowloon (Tsim Sha Tsui/Mong Kok)",
          "Novos Territórios (Sha Tin/Tsuen Wan/Tuen Mun)",
          "Ilha Lantau"
        ]
      }
    ]
  },
  {
    "name": "Hungria",
    "code": "HU",
    "states": [
      {
        "name": "Budapeste",
        "code": "BU",
        "cities": [
          "Budapeste"
        ]
      },
      {
        "name": "Peste",
        "code": "PE",
        "cities": [
          "Érd",
          "Dunakeszi",
          "Szigetszentmiklós",
          "Cegléd",
          "Vác",
          "Gödöllő",
          "Budaörs",
          "Szentendre"
        ]
      },
      {
        "name": "Hajdú-Bihar",
        "code": "HB",
        "cities": [
          "Debrecen",
          "Hajdúböszörmény",
          "Hajdúszoboszló",
          "Balmazújváros"
        ]
      },
      {
        "name": "Csongrád-Csanád",
        "code": "CS",
        "cities": [
          "Szeged",
          "Hódmezővásárhely",
          "Szentes",
          "Makó"
        ]
      },
      {
        "name": "Borsod-Abaúj-Zemplén",
        "code": "BA",
        "cities": [
          "Miskolc",
          "Ózd",
          "Kazincbarcika",
          "Mezőkövesd"
        ]
      },
      {
        "name": "Győr-Moson-Sopron",
        "code": "GS",
        "cities": [
          "Győr",
          "Sopron",
          "Mosonmagyaróvár"
        ]
      },
      {
        "name": "Baranya",
        "code": "BA",
        "cities": [
          "Pécs",
          "Komló",
          "Mohács",
          "Szigetvár"
        ]
      },
      {
        "name": "Fejér",
        "code": "FE",
        "cities": [
          "Székesfehérvár",
          "Dunaújváros",
          "Mór"
        ]
      },
      {
        "name": "Bács-Kiskun",
        "code": "BK",
        "cities": [
          "Kecskemét",
          "Baja",
          "Kiskunfélegyháza",
          "Kiskunhalas"
        ]
      },
      {
        "name": "Szabolcs-Szatmár-Bereg",
        "code": "SZ",
        "cities": [
          "Nyíregyháza",
          "Mátészalka",
          "Kisvárda"
        ]
      },
      {
        "name": "Vas",
        "code": "VA",
        "cities": [
          "Szombathely",
          "Sárvár",
          "Körmend"
        ]
      },
      {
        "name": "Heves",
        "code": "HE",
        "cities": [
          "Eger",
          "Gyöngyös",
          "Hatvan"
        ]
      },
      {
        "name": "Veszprém",
        "code": "VE",
        "cities": [
          "Veszprém",
          "Pápa",
          "Ajka",
          "Várpalota",
          "Balatonfüred"
        ]
      }
    ]
  },
  {
    "name": "Iêmen",
    "code": "YE",
    "states": [
      {
        "name": "Sana",
        "code": "SA",
        "cities": [
          "Sana",
          "Áden",
          "Taiz",
          "Al-Hudaida",
          "Mukalla"
        ]
      }
    ]
  },
  {
    "name": "Ilhas Cayman",
    "code": "KY",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "George Town",
          "West Bay",
          "Bodden Town"
        ]
      }
    ]
  },
  {
    "name": "Ilhas Cook",
    "code": "CK",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Avarua",
          "Amuri",
          "Arutanga"
        ]
      }
    ]
  },
  {
    "name": "Ilhas Feroe",
    "code": "FO",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Tórshavn",
          "Klaksvík",
          "Hoyvík",
          "Argir"
        ]
      }
    ]
  },
  {
    "name": "Ilhas Marshall",
    "code": "MH",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Majuro",
          "Ebeye",
          "Arno"
        ]
      }
    ]
  },
  {
    "name": "Ilhas Salomão",
    "code": "SB",
    "states": [
      {
        "name": "Ilhas Salomão",
        "code": "SB",
        "cities": [
          "Honiara",
          "Gizo",
          "Auki",
          "Noro"
        ]
      }
    ]
  },
  {
    "name": "Índia",
    "code": "IN",
    "states": [
      {
        "name": "Maharashtra",
        "code": "MH",
        "cities": [
          "Mumbai",
          "Pune",
          "Nagpur",
          "Thane",
          "Pimpri-Chinchwad",
          "Nashik",
          "Kalyan-Dombivli",
          "Vasai-Virar",
          "Navi Mumbai",
          "Aurangabad (Chhatrapati Sambhajinagar)",
          "Solapur",
          "Kolhapur"
        ]
      },
      {
        "name": "Délhi (NCR)",
        "code": "DL",
        "cities": [
          "Nova Délhi",
          "Délhi",
          "Noida",
          "Gurugram (Gurgaon)",
          "Faridabad",
          "Ghaziabad"
        ]
      },
      {
        "name": "Karnataka",
        "code": "KA",
        "cities": [
          "Bangalore (Bengaluru)",
          "Mysore (Mysuru)",
          "Hubli-Dharwad",
          "Mangalore (Mangaluru)",
          "Belgaum",
          "Gulbarga"
        ]
      },
      {
        "name": "Telangana",
        "code": "TG",
        "cities": [
          "Hyderabad",
          "Warangal",
          "Nizamabad",
          "Karimnagar",
          "Khammam"
        ]
      },
      {
        "name": "Tamil Nadu",
        "code": "TN",
        "cities": [
          "Chennai",
          "Coimbatore",
          "Madurai",
          "Tiruchirappalli",
          "Salem",
          "Tiruppur",
          "Erode",
          "Vellore"
        ]
      },
      {
        "name": "Gujarat",
        "code": "GJ",
        "cities": [
          "Ahmedabad",
          "Surat",
          "Vadodara",
          "Rajkot",
          "Bhavnagar",
          "Jamnagar",
          "Gandhinagar"
        ]
      },
      {
        "name": "Bengala Ocidental",
        "code": "WB",
        "cities": [
          "Calcutá (Kolkata)",
          "Howrah",
          "Siliguri",
          "Durgapur",
          "Asansol",
          "Kharagpur"
        ]
      },
      {
        "name": "Rajastão",
        "code": "RJ",
        "cities": [
          "Jaipur",
          "Jodhpur",
          "Kota",
          "Bikaner",
          "Ajmer",
          "Udaipur",
          "Bhilwara"
        ]
      },
      {
        "name": "Uttar Pradesh",
        "code": "UP",
        "cities": [
          "Lucknow",
          "Kanpur",
          "Agra",
          "Varanasi",
          "Prayagraj (Allahabad)",
          "Meerut",
          "Bareilly",
          "Aligarh",
          "Moradabad"
        ]
      },
      {
        "name": "Kerala",
        "code": "KL",
        "cities": [
          "Kochi",
          "Thiruvananthapuram",
          "Kozhikode",
          "Thrissur",
          "Kollam",
          "Kannur"
        ]
      },
      {
        "name": "Punjab",
        "code": "PB",
        "cities": [
          "Ludhiana",
          "Amritsar",
          "Jalandhar",
          "Patiala",
          "Bathinda",
          "Mohali"
        ]
      },
      {
        "name": "Madhya Pradesh",
        "code": "MP",
        "cities": [
          "Indore",
          "Bhopal",
          "Jabalpur",
          "Gwalior",
          "Ujjain"
        ]
      },
      {
        "name": "Andhra Pradesh",
        "code": "AP",
        "cities": [
          "Visakhapatnam",
          "Vijayawada",
          "Guntur",
          "Nellore",
          "Kurnool",
          "Tirupati"
        ]
      }
    ]
  },
  {
    "name": "Indonésia",
    "code": "ID",
    "states": [
      {
        "name": "Jacarta (DKI Jakarta)",
        "code": "JK",
        "cities": [
          "Jacarta Central",
          "Jacarta do Sul",
          "Jacarta Ocidental",
          "Jacarta Oriental",
          "Jacarta do Norte"
        ]
      },
      {
        "name": "Java Ocidental",
        "code": "JB",
        "cities": [
          "Bandung",
          "Bekasi",
          "Depok",
          "Bogor",
          "Cimahi",
          "Tasikmalaya",
          "Cirebon",
          "Sukabumi"
        ]
      },
      {
        "name": "Java Oriental",
        "code": "JI",
        "cities": [
          "Surabaya",
          "Malang",
          "Sidoarjo",
          "Jember",
          "Kediri",
          "Banyuwangi",
          "Probolinggo",
          "Madiun",
          "Pasuruan",
          "Batu"
        ]
      },
      {
        "name": "Java Central",
        "code": "JT",
        "cities": [
          "Semarang",
          "Surakarta (Solo)",
          "Pekalongan",
          "Tegal",
          "Magelang",
          "Salatiga",
          "Purwokerto"
        ]
      },
      {
        "name": "Banten",
        "code": "BT",
        "cities": [
          "Tangerang",
          "South Tangerang (Tangerang Selatan)",
          "Serang",
          "Cilegon"
        ]
      },
      {
        "name": "Yogyakarta",
        "code": "YO",
        "cities": [
          "Yogyakarta",
          "Sleman",
          "Bantul"
        ]
      },
      {
        "name": "Bali",
        "code": "BA",
        "cities": [
          "Denpasar",
          "Badung (Kuta/Canggu/Seminyak)",
          "Gianyar (Ubud)",
          "Singaraja",
          "Tabanan"
        ]
      },
      {
        "name": "Sumatra do Norte",
        "code": "SU",
        "cities": [
          "Medan",
          "Pematangsiantar",
          "Binjai",
          "Tebing Tinggi",
          "Padang Sidempuan"
        ]
      },
      {
        "name": "Sumatra do Sul",
        "code": "SS",
        "cities": [
          "Palembang",
          "Prabumulih",
          "Lubuklinggau"
        ]
      },
      {
        "name": "Riau",
        "code": "RI",
        "cities": [
          "Pekanbaru",
          "Dumai",
          "Batam (Ilhas Riau)"
        ]
      },
      {
        "name": "Sulawesi do Sul",
        "code": "SN",
        "cities": [
          "Makassar",
          "Palopo",
          "Parepare"
        ]
      },
      {
        "name": "Kalimantan Oriental",
        "code": "KI",
        "cities": [
          "Balikpapan",
          "Samarinda",
          "Bontang",
          "Nusantara (IKN)"
        ]
      }
    ]
  },
  {
    "name": "Irã",
    "code": "IR",
    "states": [
      {
        "name": "Teerã",
        "code": "07",
        "cities": [
          "Teerã",
          "Rey",
          "Shemiranat",
          "Eslamshahr",
          "Karaj"
        ]
      },
      {
        "name": "Razavi Khorasan",
        "code": "09",
        "cities": [
          "Mashhad",
          "Nishapur",
          "Sabzevar"
        ]
      },
      {
        "name": "Isfahan",
        "code": "04",
        "cities": [
          "Isfahan",
          "Kashan",
          "Khomeyni Shahr",
          "Najafabad"
        ]
      },
      {
        "name": "Fars",
        "code": "14",
        "cities": [
          "Shiraz",
          "Marvdasht",
          "Jahrom"
        ]
      },
      {
        "name": "Azerbaijão Oriental",
        "code": "01",
        "cities": [
          "Tabriz",
          "Maragheh",
          "Marand"
        ]
      },
      {
        "name": "Khuzistão",
        "code": "10",
        "cities": [
          "Ahvaz",
          "Abadan",
          "Dezful",
          "Khorramshahr"
        ]
      }
    ]
  },
  {
    "name": "Iraque",
    "code": "IQ",
    "states": [
      {
        "name": "Bagdá",
        "code": "BG",
        "cities": [
          "Bagdá",
          "Karkh",
          "Rusafa",
          "Sadr City"
        ]
      },
      {
        "name": "Basra",
        "code": "BA",
        "cities": [
          "Basra",
          "Al-Zubair",
          "Al-Qurna"
        ]
      },
      {
        "name": "Erbil (Curdistão)",
        "code": "AR",
        "cities": [
          "Erbil",
          "Soran"
        ]
      },
      {
        "name": "Suleimânia",
        "code": "SU",
        "cities": [
          "Suleimânia",
          "Kalar"
        ]
      },
      {
        "name": "Mosul (Nínive)",
        "code": "NI",
        "cities": [
          "Mosul",
          "Tel Afar"
        ]
      },
      {
        "name": "Najaf",
        "code": "NA",
        "cities": [
          "Najaf",
          "Kufa"
        ]
      }
    ]
  },
  {
    "name": "Irlanda",
    "code": "IE",
    "states": [
      {
        "name": "Leinster",
        "code": "L",
        "cities": [
          "Dublin",
          "Dún Laoghaire",
          "Drogheda",
          "Dundalk",
          "Bray",
          "Navan",
          "Kilkenny",
          "Naas",
          "Carlow",
          "Wexford",
          "Mullingar",
          "Athlone"
        ]
      },
      {
        "name": "Munster",
        "code": "M",
        "cities": [
          "Cork",
          "Limerick",
          "Waterford",
          "Ennis",
          "Tralee",
          "Killarney",
          "Clonmel",
          "Cobh",
          "Mallow"
        ]
      },
      {
        "name": "Connacht",
        "code": "C",
        "cities": [
          "Galway",
          "Sligo",
          "Castlebar",
          "Ballina",
          "Tuam",
          "Roscommon"
        ]
      },
      {
        "name": "Ulster (República)",
        "code": "U",
        "cities": [
          "Letterkenny",
          "Cavan",
          "Monaghan",
          "Buncrana"
        ]
      }
    ]
  },
  {
    "name": "Islândia",
    "code": "IS",
    "states": [
      {
        "name": "Região da Capital",
        "code": "1",
        "cities": [
          "Reykjavík",
          "Kópavogur",
          "Hafnarfjörður",
          "Garðabær",
          "Mosfellsbær",
          "Akranes",
          "Akureyri",
          "Keflavík",
          "Selfoss"
        ]
      }
    ]
  },
  {
    "name": "Israel",
    "code": "IL",
    "states": [
      {
        "name": "Tel Aviv",
        "code": "TA",
        "cities": [
          "Tel Aviv-Yafo",
          "Holon",
          "Bnei Brak",
          "Bat Yam",
          "Ramat Gan",
          "Herzliya",
          "Ramat HaSharon"
        ]
      },
      {
        "name": "Jerusalém",
        "code": "JM",
        "cities": [
          "Jerusalém",
          "Beit Shemesh",
          "Mevaseret Zion"
        ]
      },
      {
        "name": "Distrito Central",
        "code": "M",
        "cities": [
          "Rishon LeZion",
          "Petah Tikva",
          "Netanya",
          "Rehovot",
          "Kfar Saba",
          "Raanana",
          "Modi'in-Maccabim-Re'ut",
          "Lod",
          "Ramla"
        ]
      },
      {
        "name": "Haifa",
        "code": "HA",
        "cities": [
          "Haifa",
          "Hadera",
          "Kiryat Ata",
          "Kiryat Motzkin",
          "Kiryat Bialik"
        ]
      },
      {
        "name": "Distrito Sul",
        "code": "D",
        "cities": [
          "Beer-Sheva",
          "Ashdod",
          "Ashkelon",
          "Eilat",
          "Dimona",
          "Sderot"
        ]
      },
      {
        "name": "Distrito Norte",
        "code": "Z",
        "cities": [
          "Nazaré",
          "Tiberíades",
          "Nahariya",
          "Acre (Akko)",
          "Afula",
          "Carmiel"
        ]
      }
    ]
  },
  {
    "name": "Itália",
    "code": "IT",
    "states": [
      {
        "name": "Lombardia",
        "code": "LOM",
        "cities": [
          "Milão",
          "Brescia",
          "Monza",
          "Bérgamo",
          "Busto Arsizio",
          "Como",
          "Sesto San Giovanni",
          "Varese",
          "Cinisello Balsamo",
          "Pavia",
          "Cremona",
          "Lecco",
          "Lodi",
          "Mântua"
        ]
      },
      {
        "name": "Lácio",
        "code": "LAZ",
        "cities": [
          "Roma",
          "Latina",
          "Guidonia Montecelio",
          "Fiumicino",
          "Aprilia",
          "Viterbo",
          "Pomezia",
          "Tivoli",
          "Anzio",
          "Civitavecchia",
          "Velletri",
          "Frosinone"
        ]
      },
      {
        "name": "Campânia",
        "code": "CAM",
        "cities": [
          "Nápoles",
          "Salerno",
          "Giugliano in Campania",
          "Torre del Greco",
          "Pozzuoli",
          "Casoria",
          "Caserta",
          "Castellammare di Stabia",
          "Afragola",
          "Benevento",
          "Avellino",
          "Acerra",
          "Ercolano"
        ]
      },
      {
        "name": "Vêneto",
        "code": "VEN",
        "cities": [
          "Veneza",
          "Verona",
          "Pádua",
          "Vicenza",
          "Treviso",
          "Rovigo",
          "Chioggia",
          "Bassano del Grappa",
          "San Donà di Piave",
          "Belluno"
        ]
      },
      {
        "name": "Emília-Romanha",
        "code": "EMR",
        "cities": [
          "Bolonha",
          "Parma",
          "Módena",
          "Reggio nell'Emilia",
          "Ravena",
          "Rimini",
          "Ferrara",
          "Forlì",
          "Piacenza",
          "Cesena",
          "Carpi",
          "Imola",
          "Faenza"
        ]
      },
      {
        "name": "Piemonte",
        "code": "PIE",
        "cities": [
          "Turim",
          "Novara",
          "Alessandria",
          "Asti",
          "Moncalieri",
          "Cuneo",
          "Collegno",
          "Rivoli",
          "Vercelli",
          "Biella"
        ]
      },
      {
        "name": "Toscana",
        "code": "TOS",
        "cities": [
          "Florença",
          "Prato",
          "Livorno",
          "Arezzo",
          "Pistoia",
          "Pisa",
          "Lucca",
          "Grosseto",
          "Massa",
          "Carrara",
          "Viareggio",
          "Siena"
        ]
      },
      {
        "name": "Sicília",
        "code": "SIC",
        "cities": [
          "Palermo",
          "Catânia",
          "Messina",
          "Siracusa",
          "Marsala",
          "Gela",
          "Ragusa",
          "Trapani",
          "Agrigento",
          "Caltanissetta"
        ]
      },
      {
        "name": "Púglia",
        "code": "PUG",
        "cities": [
          "Bari",
          "Taranto",
          "Foggia",
          "Andria",
          "Lecce",
          "Barletta",
          "Brindisi",
          "Altamura",
          "Molfetta",
          "Cerignola",
          "Trani"
        ]
      },
      {
        "name": "Ligúria",
        "code": "LIG",
        "cities": [
          "Gênova",
          "La Spezia",
          "Savona",
          "Sanremo",
          "Imperia"
        ]
      },
      {
        "name": "Sardenha",
        "code": "SAR",
        "cities": [
          "Cagliari",
          "Sassari",
          "Quartu Sant'Elena",
          "Olbia",
          "Alghero",
          "Nuoro",
          "Oristano"
        ]
      },
      {
        "name": "Calábria",
        "code": "CAL",
        "cities": [
          "Reggio Calabria",
          "Catanzaro",
          "Corigliano-Rossano",
          "Lamezia Terme",
          "Cosenza",
          "Crotone"
        ]
      },
      {
        "name": "Friuli-Venezia Giulia",
        "code": "FVG",
        "cities": [
          "Trieste",
          "Udine",
          "Pordenone",
          "Gorizia"
        ]
      },
      {
        "name": "Trentino-Alto Ádige",
        "code": "TAA",
        "cities": [
          "Trento",
          "Bolzano",
          "Rovereto",
          "Merano"
        ]
      },
      {
        "name": "Úmbria",
        "code": "UMB",
        "cities": [
          "Perugia",
          "Terni",
          "Foligno",
          "Città di Castello",
          "Spoleto",
          "Assisi"
        ]
      },
      {
        "name": "Marcas",
        "code": "MAR",
        "cities": [
          "Ancona",
          "Pesaro",
          "Fano",
          "San Benedetto del Tronto",
          "Ascoli Piceno",
          "Macerata"
        ]
      },
      {
        "name": "Abruzzo",
        "code": "ABR",
        "cities": [
          "Pescara",
          "L'Aquila",
          "Montesilvano",
          "Teramo",
          "Chieti"
        ]
      }
    ]
  },
  {
    "name": "Jamaica",
    "code": "JM",
    "states": [
      {
        "name": "Kingston",
        "code": "01",
        "cities": [
          "Kingston",
          "Portmore",
          "Spanish Town",
          "Montego Bay",
          "Mandeville",
          "Ocho Rios"
        ]
      }
    ]
  },
  {
    "name": "Japão",
    "code": "JP",
    "states": [
      {
        "name": "Tóquio",
        "code": "13",
        "cities": [
          "Tóquio",
          "Shinjuku",
          "Shibuya",
          "Chiyoda",
          "Minato",
          "Chuo",
          "Setagaya",
          "Shinagawa",
          "Koto",
          "Meguro",
          "Taito (Akihabara)",
          "Toshima (Ikebukuro)",
          "Nakano",
          "Hachioji",
          "Machida"
        ]
      },
      {
        "name": "Osaka",
        "code": "27",
        "cities": [
          "Osaka",
          "Sakai",
          "Higashiosaka",
          "Toyonaka",
          "Hirakata",
          "Suita",
          "Takatsuki",
          "Ibaraki",
          "Yao"
        ]
      },
      {
        "name": "Kanagawa",
        "code": "14",
        "cities": [
          "Yokohama",
          "Kawasaki",
          "Sagamihara",
          "Yokosuka",
          "Fujisawa",
          "Chigasaki",
          "Kamakura",
          "Odawara"
        ]
      },
      {
        "name": "Aichi",
        "code": "23",
        "cities": [
          "Nagoya",
          "Toyota",
          "Toyohashi",
          "Okazaki",
          "Ichinomiya",
          "Kasugai",
          "Anjo"
        ]
      },
      {
        "name": "Fukuoka",
        "code": "40",
        "cities": [
          "Fukuoka",
          "Kitakyushu",
          "Kurume",
          "Iizuka",
          "Omuta",
          "Kasuga"
        ]
      },
      {
        "name": "Hokkaido",
        "code": "01",
        "cities": [
          "Sapporo",
          "Asahikawa",
          "Hakodate",
          "Kushiro",
          "Tomakomai",
          "Obihiro",
          "Otaru"
        ]
      },
      {
        "name": "Hyogo",
        "code": "28",
        "cities": [
          "Kobe",
          "Himeji",
          "Nishinomiya",
          "Amagasaki",
          "Akashi",
          "Kakogawa",
          "Takarazuka"
        ]
      },
      {
        "name": "Quioto",
        "code": "26",
        "cities": [
          "Quioto",
          "Uji",
          "Kameoka",
          "Maizuru",
          "Joyo",
          "Nagaokakyo"
        ]
      },
      {
        "name": "Saitama",
        "code": "11",
        "cities": [
          "Saitama",
          "Kawaguchi",
          "Kawagoe",
          "Tokorozawa",
          "Koshigaya",
          "Soka",
          "Kasukabe"
        ]
      },
      {
        "name": "Chiba",
        "code": "12",
        "cities": [
          "Chiba",
          "Funabashi",
          "Matsudo",
          "Ichikawa",
          "Kashiwa",
          "Ichihara",
          "Narita",
          "Urayasu"
        ]
      },
      {
        "name": "Hiroshima",
        "code": "34",
        "cities": [
          "Hiroshima",
          "Fukuyama",
          "Kure",
          "Higashihiroshima",
          "Onomichi"
        ]
      },
      {
        "name": "Miyagi",
        "code": "04",
        "cities": [
          "Sendai",
          "Ishinomaki",
          "Osaki",
          "Natori"
        ]
      },
      {
        "name": "Shizuoka",
        "code": "22",
        "cities": [
          "Shizuoka",
          "Hamamatsu",
          "Fuji",
          "Numazu",
          "Iwata"
        ]
      },
      {
        "name": "Okinawa",
        "code": "47",
        "cities": [
          "Naha",
          "Okinawa",
          "Uruma",
          "Urasoe",
          "Nago"
        ]
      }
    ]
  },
  {
    "name": "Jordânia",
    "code": "JO",
    "states": [
      {
        "name": "Amã",
        "code": "AM",
        "cities": [
          "Amã",
          "Wadi As-Ser",
          "Al-Jubaiha",
          "Sahab"
        ]
      },
      {
        "name": "Zarqa",
        "code": "AZ",
        "cities": [
          "Zarqa",
          "Russeifa"
        ]
      },
      {
        "name": "Irbid",
        "code": "IR",
        "cities": [
          "Irbid",
          "Ar-Ramtha"
        ]
      },
      {
        "name": "Aqaba",
        "code": "AQ",
        "cities": [
          "Aqaba"
        ]
      }
    ]
  },
  {
    "name": "Kosovo",
    "code": "XK",
    "states": [
      {
        "name": "Kosovo",
        "code": "XK",
        "cities": [
          "Pristina",
          "Prizren",
          "Peć (Peja)",
          "Gjilan",
          "Mitrovica",
          "Ferizaj",
          "Gjakova"
        ]
      }
    ]
  },
  {
    "name": "Kuwait",
    "code": "KW",
    "states": [
      {
        "name": "Al Asimah (Capital)",
        "code": "KU",
        "cities": [
          "Cidade do Kuwait",
          "Hawalli",
          "Salmiya",
          "Al Farwaniyah",
          "Al Ahmadi",
          "Jahra",
          "Mubarak Al-Kabeer"
        ]
      }
    ]
  },
  {
    "name": "Laos",
    "code": "LA",
    "states": [
      {
        "name": "Vientiane",
        "code": "VT",
        "cities": [
          "Vientiane",
          "Pakse",
          "Savannakhet",
          "Luang Prabang"
        ]
      }
    ]
  },
  {
    "name": "Lesoto",
    "code": "LS",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Maseru",
          "Teyateyaneng",
          "Mafeteng",
          "Hlotse"
        ]
      }
    ]
  },
  {
    "name": "Letônia",
    "code": "LV",
    "states": [
      {
        "name": "Riga",
        "code": "RIX",
        "cities": [
          "Riga",
          "Jūrmala",
          "Jelgava",
          "Daugavpils",
          "Liepāja",
          "Ventspils",
          "Rēzekne",
          "Valmiera",
          "Jēkabpils",
          "Ogre",
          "Salaspils",
          "Tukums",
          "Cēsis"
        ]
      }
    ]
  },
  {
    "name": "Líbano",
    "code": "LB",
    "states": [
      {
        "name": "Beirute",
        "code": "BA",
        "cities": [
          "Beirute"
        ]
      },
      {
        "name": "Monte Líbano",
        "code": "JL",
        "cities": [
          "Jounieh",
          "Baabda",
          "Jbeil (Biblos)",
          "Aley"
        ]
      },
      {
        "name": "Norte",
        "code": "AS",
        "cities": [
          "Trípoli",
          "Zgharta",
          "Batroun"
        ]
      },
      {
        "name": "Sul",
        "code": "JA",
        "cities": [
          "Sídon (Saida)",
          "Tiro (Sour)"
        ]
      }
    ]
  },
  {
    "name": "Libéria",
    "code": "LR",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Monróvia",
          "Gbarnga",
          "Buchanan",
          "Ganta",
          "Kakata"
        ]
      }
    ]
  },
  {
    "name": "Líbia",
    "code": "LY",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Trípoli",
          "Bengasi",
          "Misrata",
          "Bayda",
          "Zawiya",
          "Zliten",
          "Tobruk"
        ]
      }
    ]
  },
  {
    "name": "Liechtenstein",
    "code": "LI",
    "states": [
      {
        "name": "Liechtenstein",
        "code": "LI",
        "cities": [
          "Vaduz",
          "Schaan",
          "Balzers",
          "Triesen",
          "Eschen"
        ]
      }
    ]
  },
  {
    "name": "Lituânia",
    "code": "LT",
    "states": [
      {
        "name": "Vilnius",
        "code": "VL",
        "cities": [
          "Vilnius",
          "Ukmergė",
          "Elektrėnai",
          "Trakai"
        ]
      },
      {
        "name": "Kaunas",
        "code": "KU",
        "cities": [
          "Kaunas",
          "Jonava",
          "Kėdainiai",
          "Raseiniai"
        ]
      },
      {
        "name": "Klaipėda",
        "code": "KL",
        "cities": [
          "Klaipėda",
          "Palanga",
          "Kretinga",
          "Šilutė",
          "Neringa"
        ]
      },
      {
        "name": "Šiauliai",
        "code": "SA",
        "cities": [
          "Šiauliai",
          "Radviliškis",
          "Kuršėnai"
        ]
      },
      {
        "name": "Panevėžys",
        "code": "PN",
        "cities": [
          "Panevėžys",
          "Rokiškis",
          "Biržai"
        ]
      },
      {
        "name": "Alytus",
        "code": "AL",
        "cities": [
          "Alytus",
          "Druskininkai",
          "Varėna"
        ]
      },
      {
        "name": "Marijampolė",
        "code": "MR",
        "cities": [
          "Marijampolė",
          "Vilkaviškis"
        ]
      }
    ]
  },
  {
    "name": "Luxemburgo",
    "code": "LU",
    "states": [
      {
        "name": "Luxemburgo",
        "code": "LU",
        "cities": [
          "Cidade de Luxemburgo",
          "Esch-sur-Alzette",
          "Differdange",
          "Dudelange",
          "Ettelbruck",
          "Diekirch"
        ]
      }
    ]
  },
  {
    "name": "Macau",
    "code": "MO",
    "states": [
      {
        "name": "Macau",
        "code": "MO",
        "cities": [
          "Macau",
          "Taipa",
          "Cotai",
          "Coloane"
        ]
      }
    ]
  },
  {
    "name": "Macedônia do Norte",
    "code": "MK",
    "states": [
      {
        "name": "Macedônia do Norte",
        "code": "MK",
        "cities": [
          "Skopje",
          "Bitola",
          "Kumanovo",
          "Prilep",
          "Tetovo",
          "Veles",
          "Ohrid",
          "Gostivar",
          "Štip",
          "Strumica"
        ]
      }
    ]
  },
  {
    "name": "Madagascar",
    "code": "MG",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Antananarivo",
          "Toamasina",
          "Antsirabe",
          "Mahajanga",
          "Fianarantsoa",
          "Toliara",
          "Antsiranana"
        ]
      }
    ]
  },
  {
    "name": "Malásia",
    "code": "MY",
    "states": [
      {
        "name": "Kuala Lumpur",
        "code": "14",
        "cities": [
          "Kuala Lumpur",
          "Bukit Bintang",
          "Cheras",
          "Kepong",
          "Setiawangsa"
        ]
      },
      {
        "name": "Selangor",
        "code": "10",
        "cities": [
          "Petaling Jaya",
          "Shah Alam",
          "Subang Jaya",
          "Klang",
          "Ampang Jaya",
          "Kajang",
          "Selayang",
          "Sepang (Cyberjaya)",
          "Puchong"
        ]
      },
      {
        "name": "Penang (Pinang)",
        "code": "07",
        "cities": [
          "George Town",
          "Butterworth",
          "Bayan Lepas",
          "Bukit Mertajam"
        ]
      },
      {
        "name": "Johor",
        "code": "01",
        "cities": [
          "Johor Bahru",
          "Iskandar Puteri",
          "Pasir Gudang",
          "Batu Pahat",
          "Muar",
          "Kluang"
        ]
      },
      {
        "name": "Perak",
        "code": "08",
        "cities": [
          "Ipoh",
          "Taiping",
          "Teluk Intan",
          "Batu Gajah"
        ]
      },
      {
        "name": "Sarawak (Bornéu)",
        "code": "13",
        "cities": [
          "Kuching",
          "Miri",
          "Sibu",
          "Bintulu"
        ]
      },
      {
        "name": "Sabah (Bornéu)",
        "code": "12",
        "cities": [
          "Kota Kinabalu",
          "Sandakan",
          "Tawau"
        ]
      },
      {
        "name": "Malaca (Melaka)",
        "code": "04",
        "cities": [
          "Cidade de Malaca",
          "Ayer Keroh",
          "Alor Gajah"
        ]
      }
    ]
  },
  {
    "name": "Malaui",
    "code": "MW",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Lilongwe",
          "Blantyre",
          "Mzuzu",
          "Zomba",
          "Kasungu"
        ]
      }
    ]
  },
  {
    "name": "Maldivas",
    "code": "MV",
    "states": [
      {
        "name": "Malé",
        "code": "MLE",
        "cities": [
          "Malé",
          "Hulhumalé",
          "Addu City",
          "Fuvahmulah"
        ]
      }
    ]
  },
  {
    "name": "Mali",
    "code": "ML",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Bamako",
          "Sikasso",
          "Mopti",
          "Koutiala",
          "Kayes",
          "Ségou"
        ]
      }
    ]
  },
  {
    "name": "Malta",
    "code": "MT",
    "states": [
      {
        "name": "Malta",
        "code": "MT",
        "cities": [
          "Valeta",
          "Birkirkara",
          "Mosta",
          "Qormi",
          "Zabbar",
          "Saint Paul's Bay",
          "Sliema",
          "San Ġiljan (St. Julian's)",
          "Gozo (Victoria)"
        ]
      }
    ]
  },
  {
    "name": "Marrocos",
    "code": "MA",
    "states": [
      {
        "name": "Casablanca-Settat",
        "code": "06",
        "cities": [
          "Casablanca",
          "Mohammedia",
          "El Jadida",
          "Settat",
          "Berrechid"
        ]
      },
      {
        "name": "Rabat-Salé-Kénitra",
        "code": "04",
        "cities": [
          "Rabat",
          "Salé",
          "Kénitra",
          "Témara",
          "Skhirat",
          "Khemisset"
        ]
      },
      {
        "name": "Marrakech-Safi",
        "code": "07",
        "cities": [
          "Marrakech",
          "Safi",
          "Essaouira",
          "El Kelaa des Sraghna"
        ]
      },
      {
        "name": "Tânger-Tetuão-Al Hoceïma",
        "code": "01",
        "cities": [
          "Tânger",
          "Tetuão",
          "Al Hoceïma",
          "Larache",
          "Ksar El Kebir",
          "Chefchaouen"
        ]
      },
      {
        "name": "Fez-Meknès",
        "code": "03",
        "cities": [
          "Fez",
          "Meknès",
          "Taza",
          "Sefrou",
          "Ifrane"
        ]
      },
      {
        "name": "Souss-Massa",
        "code": "09",
        "cities": [
          "Agadir",
          "Inezgane",
          "Taroudant",
          "Tiznit",
          "Oulad Teima"
        ]
      },
      {
        "name": "Oriental",
        "code": "02",
        "cities": [
          "Oujda",
          "Nador",
          "Berkane",
          "Taourirt"
        ]
      }
    ]
  },
  {
    "name": "Maurício",
    "code": "MU",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Port Louis",
          "Beau Bassin-Rose Hill",
          "Vacoas-Phoenix",
          "Curepipe",
          "Quatre Bornes",
          "Grand Baie"
        ]
      }
    ]
  },
  {
    "name": "Mauritânia",
    "code": "MR",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Nouakchott",
          "Nouadhibou",
          "Kiffa",
          "Kaédi",
          "Rosso"
        ]
      }
    ]
  },
  {
    "name": "México",
    "code": "MX",
    "states": [
      {
        "name": "Cidade do México (CDMX)",
        "code": "CMX",
        "cities": [
          "Cidade do México",
          "Coyoacán",
          "Cuauhtémoc",
          "Gustavo A. Madero",
          "Iztapalapa",
          "Miguel Hidalgo",
          "Benito Juárez",
          "Tlalpan",
          "Álvaro Obregón"
        ]
      },
      {
        "name": "Jalisco",
        "code": "JAL",
        "cities": [
          "Guadalajara",
          "Zapopan",
          "Tlaquepaque",
          "Tonalá",
          "Puerto Vallarta",
          "Tlajomulco de Zúñiga",
          "Lagos de Moreno",
          "Ciudad Guzmán"
        ]
      },
      {
        "name": "Nuevo León",
        "code": "NLE",
        "cities": [
          "Monterrey",
          "Guadalupe",
          "San Pedro Garza García",
          "San Nicolás de los Garza",
          "Apodaca",
          "General Escobedo",
          "Santa Catarina",
          "Juárez"
        ]
      },
      {
        "name": "Estado do México",
        "code": "MEX",
        "cities": [
          "Toluca",
          "Ecatepec",
          "Nezahualcóyotl",
          "Naucalpan",
          "Tlalnepantla",
          "Chimalhuacán",
          "Cuautitlán Izcalli",
          "Atizapán de Zaragoza",
          "Ixtapaluca",
          "Metepec"
        ]
      },
      {
        "name": "Puebla",
        "code": "PUE",
        "cities": [
          "Puebla",
          "Tehuacán",
          "San Martín Texmelucan",
          "Cholula",
          "Atlixco",
          "Teziutlán"
        ]
      },
      {
        "name": "Guanajuato",
        "code": "GUA",
        "cities": [
          "León",
          "Irapuato",
          "Celaya",
          "Salamanca",
          "Guanajuato",
          "San Miguel de Allende",
          "Silao"
        ]
      },
      {
        "name": "Veracruz",
        "code": "VER",
        "cities": [
          "Veracruz",
          "Xalapa",
          "Coatzacoalcos",
          "Poza Rica",
          "Córdoba",
          "Boca del Río",
          "Minatitlán",
          "Orizaba"
        ]
      },
      {
        "name": "Baja California",
        "code": "BCN",
        "cities": [
          "Tijuana",
          "Mexicali",
          "Ensenada",
          "Tecate",
          "Playas de Rosarito"
        ]
      },
      {
        "name": "Chihuahua",
        "code": "CHH",
        "cities": [
          "Ciudad Juárez",
          "Chihuahua",
          "Cuauhtémoc",
          "Delicias",
          "Hidalgo del Parral"
        ]
      },
      {
        "name": "Querétaro",
        "code": "QUE",
        "cities": [
          "Santiago de Querétaro",
          "San Juan del Río",
          "El Marqués",
          "Corregidora"
        ]
      },
      {
        "name": "Yucatán",
        "code": "YUC",
        "cities": [
          "Mérida",
          "Kanasín",
          "Valladolid",
          "Tizimín",
          "Progreso"
        ]
      },
      {
        "name": "Quintana Roo",
        "code": "ROO",
        "cities": [
          "Cancún",
          "Playa del Carmen",
          "Chetumal",
          "Cozumel",
          "Tulum"
        ]
      },
      {
        "name": "Sonora",
        "code": "SON",
        "cities": [
          "Hermosillo",
          "Ciudad Obregón",
          "Nogales",
          "San Luis Río Colorado",
          "Navojoa",
          "Guaymas"
        ]
      },
      {
        "name": "Sinaloa",
        "code": "SIN",
        "cities": [
          "Culiacán",
          "Mazatlán",
          "Los Mochis",
          "Guasave",
          "Navolato"
        ]
      },
      {
        "name": "Coahuila",
        "code": "COA",
        "cities": [
          "Saltillo",
          "Torreón",
          "Monclova",
          "Piedras Negras",
          "Acuña"
        ]
      },
      {
        "name": "San Luis Potosí",
        "code": "SLP",
        "cities": [
          "San Luis Potosí",
          "Soledad de Graciano Sánchez",
          "Ciudad Valles",
          "Matehuala"
        ]
      },
      {
        "name": "Michoacán",
        "code": "MIC",
        "cities": [
          "Morelia",
          "Uruapan",
          "Zamora",
          "Lázaro Cárdenas",
          "Zitácuaro"
        ]
      },
      {
        "name": "Tamaulipas",
        "code": "TAM",
        "cities": [
          "Reynosa",
          "Matamoros",
          "Nuevo Laredo",
          "Ciudad Victoria",
          "Tampico",
          "Ciudad Madero"
        ]
      },
      {
        "name": "Aguascalientes",
        "code": "AGU",
        "cities": [
          "Aguascalientes",
          "Jesús María",
          "Calvillo"
        ]
      },
      {
        "name": "Hidalgo",
        "code": "HID",
        "cities": [
          "Pachuca",
          "Tulancingo",
          "Tula de Allende",
          "Tizayuca"
        ]
      },
      {
        "name": "Morelos",
        "code": "MOR",
        "cities": [
          "Cuernavaca",
          "Jiutepec",
          "Cuautla",
          "Temixco"
        ]
      },
      {
        "name": "Oaxaca",
        "code": "OAX",
        "cities": [
          "Oaxaca de Juárez",
          "San Juan Bautista Tuxtepec",
          "Salina Cruz",
          "Juchitán de Zaragoza"
        ]
      },
      {
        "name": "Chiapas",
        "code": "CHP",
        "cities": [
          "Tuxtla Gutiérrez",
          "Tapachula",
          "San Cristóbal de las Casas",
          "Comitán"
        ]
      },
      {
        "name": "Guerrero",
        "code": "GRO",
        "cities": [
          "Acapulco",
          "Chilpancingo",
          "Iguala",
          "Zihuatanejo"
        ]
      },
      {
        "name": "Tabasco",
        "code": "TAB",
        "cities": [
          "Villahermosa",
          "Cárdenas",
          "Comalcalco",
          "Huimanguillo"
        ]
      },
      {
        "name": "Durango",
        "code": "DUR",
        "cities": [
          "Durango",
          "Gómez Palacio",
          "Lerdo"
        ]
      },
      {
        "name": "Zacatecas",
        "code": "ZAC",
        "cities": [
          "Zacatecas",
          "Guadalupe",
          "Fresnillo",
          "Jerez"
        ]
      },
      {
        "name": "Baja California Sur",
        "code": "BCS",
        "cities": [
          "La Paz",
          "San José del Cabo",
          "Cabo San Lucas",
          "Ciudad Constitución"
        ]
      },
      {
        "name": "Nayarit",
        "code": "NAY",
        "cities": [
          "Tepic",
          "Xalisco",
          "Bahía de Banderas"
        ]
      },
      {
        "name": "Campeche",
        "code": "CAM",
        "cities": [
          "San Francisco de Campeche",
          "Ciudad del Carmen",
          "Champotón"
        ]
      },
      {
        "name": "Colima",
        "code": "COL",
        "cities": [
          "Colima",
          "Manzanillo",
          "Villa de Álvarez",
          "Tecomán"
        ]
      },
      {
        "name": "Tlaxcala",
        "code": "TLA",
        "cities": [
          "Tlaxcala",
          "Apizaco",
          "Huamantla",
          "San Pablo del Monte"
        ]
      }
    ]
  },
  {
    "name": "Mianmar (Birmânia)",
    "code": "MM",
    "states": [
      {
        "name": "Yangon",
        "code": "06",
        "cities": [
          "Yangon (Rangum)",
          "Mandalay",
          "Naypyidaw",
          "Bago",
          "Mawlamyine",
          "Taunggyi"
        ]
      }
    ]
  },
  {
    "name": "Micronésia",
    "code": "FM",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Palikir",
          "Weno",
          "Kolonia",
          "Tofol"
        ]
      }
    ]
  },
  {
    "name": "Moçambique",
    "code": "MZ",
    "states": [
      {
        "name": "Maputo (Cidade)",
        "code": "MPM",
        "cities": [
          "Maputo",
          "KaMpfumo",
          "Nlhamankulu",
          "KaMaxakeni",
          "KaMavota",
          "KaMubukwana",
          "Kanyaka"
        ]
      },
      {
        "name": "Maputo (Província)",
        "code": "L",
        "cities": [
          "Matola",
          "Boane",
          "Manhiça",
          "Marracuene",
          "Namaacha"
        ]
      },
      {
        "name": "Sofala",
        "code": "S",
        "cities": [
          "Beira",
          "Dondo",
          "Nhamatanda",
          "Buzi"
        ]
      },
      {
        "name": "Nampula",
        "code": "N",
        "cities": [
          "Nampula",
          "Nacala",
          "Ilha de Moçambique",
          "Angoche",
          "Monapo"
        ]
      },
      {
        "name": "Zambézia",
        "code": "Q",
        "cities": [
          "Quelimane",
          "Mocuba",
          "Gurué",
          "Alto Molócue"
        ]
      },
      {
        "name": "Tete",
        "code": "T",
        "cities": [
          "Tete",
          "Moatize",
          "Angónia",
          "Cahora Bassa"
        ]
      },
      {
        "name": "Manica",
        "code": "B",
        "cities": [
          "Chimoio",
          "Manica",
          "Gondola"
        ]
      },
      {
        "name": "Inhambane",
        "code": "I",
        "cities": [
          "Inhambane",
          "Maxixe",
          "Vilankulo",
          "Massinga"
        ]
      },
      {
        "name": "Gaza",
        "code": "G",
        "cities": [
          "Xai-Xai",
          "Chókwè",
          "Chibuto",
          "Bilene"
        ]
      },
      {
        "name": "Cabo Delgado",
        "code": "P",
        "cities": [
          "Pemba",
          "Montepuez",
          "Mocímboa da Praia",
          "Palma"
        ]
      },
      {
        "name": "Niassa",
        "code": "A",
        "cities": [
          "Lichinga",
          "Cuamba",
          "Mandimba"
        ]
      }
    ]
  },
  {
    "name": "Moldávia",
    "code": "MD",
    "states": [
      {
        "name": "Moldávia",
        "code": "MD",
        "cities": [
          "Chișinău",
          "Bălți",
          "Tiraspol",
          "Bender (Tighina)",
          "Rîbnița",
          "Cahul",
          "Ungheni",
          "Soroca"
        ]
      }
    ]
  },
  {
    "name": "Mônaco",
    "code": "MC",
    "states": [
      {
        "name": "Mônaco",
        "code": "MC",
        "cities": [
          "Monaco-Ville",
          "Monte Carlo",
          "La Condamine",
          "Fontvieille"
        ]
      }
    ]
  },
  {
    "name": "Mongólia",
    "code": "MN",
    "states": [
      {
        "name": "Ulan Bator",
        "code": "1",
        "cities": [
          "Ulan Bator",
          "Erdenet",
          "Darkhan",
          "Choibalsan",
          "Mörön"
        ]
      }
    ]
  },
  {
    "name": "Montenegro",
    "code": "ME",
    "states": [
      {
        "name": "Montenegro",
        "code": "ME",
        "cities": [
          "Podgorica",
          "Nikšić",
          "Herceg Novi",
          "Bar",
          "Budva",
          "Bijelo Polje",
          "Cetinje",
          "Kotor",
          "Tivat"
        ]
      }
    ]
  },
  {
    "name": "Namíbia",
    "code": "NA",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Windhoek",
          "Rundu",
          "Walvis Bay",
          "Swakopmund",
          "Oshakati"
        ]
      }
    ]
  },
  {
    "name": "Nauru",
    "code": "NR",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Yaren",
          "Denigomodu",
          "Meneng",
          "Aiwo"
        ]
      }
    ]
  },
  {
    "name": "Nepal",
    "code": "NP",
    "states": [
      {
        "name": "Bagmati",
        "code": "P3",
        "cities": [
          "Catmandu",
          "Pokhara",
          "Lalitpur (Patan)",
          "Bharatpur",
          "Bhaktapur",
          "Biratnagar",
          "Birgunj"
        ]
      }
    ]
  },
  {
    "name": "Nicarágua",
    "code": "NI",
    "states": [
      {
        "name": "Manágua",
        "code": "MN",
        "cities": [
          "Manágua",
          "Tipitapa",
          "Ciudad Sandino"
        ]
      },
      {
        "name": "León",
        "code": "LE",
        "cities": [
          "León",
          "Nagarote"
        ]
      },
      {
        "name": "Matagalpa",
        "code": "MT",
        "cities": [
          "Matagalpa",
          "Sébaco"
        ]
      },
      {
        "name": "Chinandega",
        "code": "CI",
        "cities": [
          "Chinandega",
          "El Viejo",
          "Corinto"
        ]
      }
    ]
  },
  {
    "name": "Níger",
    "code": "NE",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Niamei",
          "Zinder",
          "Maradi",
          "Agadez",
          "Tahoua"
        ]
      }
    ]
  },
  {
    "name": "Nigéria",
    "code": "NG",
    "states": [
      {
        "name": "Lagos",
        "code": "LA",
        "cities": [
          "Lagos",
          "Ikeja",
          "Lekki",
          "Victoria Island",
          "Surulere",
          "Ikorodu",
          "Alimosho",
          "Yaba",
          "Apapa"
        ]
      },
      {
        "name": "Território da Capital Federal",
        "code": "FC",
        "cities": [
          "Abuja",
          "Garki",
          "Wuse",
          "Maitama",
          "Gwarinpa"
        ]
      },
      {
        "name": "Kano",
        "code": "KN",
        "cities": [
          "Kano",
          "Fagge",
          "Dala",
          "Gwale"
        ]
      },
      {
        "name": "Rivers",
        "code": "RI",
        "cities": [
          "Port Harcourt",
          "Obio-Akpor",
          "Bonny"
        ]
      },
      {
        "name": "Oyo",
        "code": "OY",
        "cities": [
          "Ibadan",
          "Ogbomosho",
          "Oyo"
        ]
      },
      {
        "name": "Kaduna",
        "code": "KD",
        "cities": [
          "Kaduna",
          "Zaria"
        ]
      },
      {
        "name": "Enugu",
        "code": "EN",
        "cities": [
          "Enugu",
          "Nsukka"
        ]
      },
      {
        "name": "Edo",
        "code": "ED",
        "cities": [
          "Benin City",
          "Uromi"
        ]
      },
      {
        "name": "Anambra",
        "code": "AN",
        "cities": [
          "Onitsha",
          "Awka",
          "Nnewi"
        ]
      }
    ]
  },
  {
    "name": "Noruega",
    "code": "NO",
    "states": [
      {
        "name": "Oslo",
        "code": "03",
        "cities": [
          "Oslo"
        ]
      },
      {
        "name": "Viken",
        "code": "30",
        "cities": [
          "Bærum",
          "Drammen",
          "Asker",
          "Lillestrøm",
          "Fredrikstad",
          "Sarpsborg",
          "Lørenskog",
          "Moss",
          "Ski",
          "Kongsberg"
        ]
      },
      {
        "name": "Vestland",
        "code": "46",
        "cities": [
          "Bergen",
          "Åsane",
          "Askøy",
          "Fjell",
          "Førde",
          "Stord"
        ]
      },
      {
        "name": "Rogaland",
        "code": "11",
        "cities": [
          "Stavanger",
          "Sandnes",
          "Haugesund",
          "Sola",
          "Time",
          "Karmøy",
          "Eigersund"
        ]
      },
      {
        "name": "Trøndelag",
        "code": "50",
        "cities": [
          "Trondheim",
          "Stjørdal",
          "Steinkjer",
          "Levanger",
          "Namsos"
        ]
      },
      {
        "name": "Agder",
        "code": "42",
        "cities": [
          "Kristiansand",
          "Arendal",
          "Grimstad",
          "Mandal",
          "Vennesla"
        ]
      },
      {
        "name": "Innlandet",
        "code": "34",
        "cities": [
          "Hamar",
          "Lillehammer",
          "Gjøvik",
          "Elverum",
          "Kongsvinger"
        ]
      },
      {
        "name": "Vestfold og Telemark",
        "code": "38",
        "cities": [
          "Tønsberg",
          "Sandefjord",
          "Skien",
          "Porsgrunn",
          "Larvik",
          "Horten"
        ]
      },
      {
        "name": "Møre og Romsdal",
        "code": "15",
        "cities": [
          "Ålesund",
          "Molde",
          "Kristiansund",
          "Ørsta"
        ]
      },
      {
        "name": "Nordland",
        "code": "18",
        "cities": [
          "Bodø",
          "Mo i Rana",
          "Narvik",
          "Mosjøen",
          "Fauske"
        ]
      },
      {
        "name": "Troms og Finnmark",
        "code": "54",
        "cities": [
          "Tromsø",
          "Harstad",
          "Alta",
          "Hammerfest",
          "Vadsø"
        ]
      }
    ]
  },
  {
    "name": "Nova Caledônia",
    "code": "NC",
    "states": [
      {
        "name": "Nova Caledônia",
        "code": "NC",
        "cities": [
          "Nouméa",
          "Le Mont-Dore",
          "Dumbéa",
          "Païta"
        ]
      }
    ]
  },
  {
    "name": "Nova Zelândia",
    "code": "NZ",
    "states": [
      {
        "name": "Ilha do Norte",
        "code": "NI",
        "cities": [
          "Auckland",
          "Wellington",
          "Hamilton",
          "Tauranga",
          "Lower Hutt",
          "Palmerston North",
          "Napier",
          "Hastings",
          "Rotorua",
          "New Plymouth",
          "Whangarei",
          "Gisborne"
        ]
      },
      {
        "name": "Ilha do Sul",
        "code": "SI",
        "cities": [
          "Christchurch",
          "Dunedin",
          "Nelson",
          "Invercargill",
          "Queenstown",
          "Timaru",
          "Blenheim",
          "Ashburton"
        ]
      }
    ]
  },
  {
    "name": "Omã",
    "code": "OM",
    "states": [
      {
        "name": "Mascate",
        "code": "MA",
        "cities": [
          "Mascate (Muscat)",
          "Seeb",
          "Bawshar",
          "Muttrah",
          "Al Amarat"
        ]
      },
      {
        "name": "Dhofar",
        "code": "ZU",
        "cities": [
          "Salalah",
          "Taqah",
          "Mirbat"
        ]
      },
      {
        "name": "Al Batinah do Norte",
        "code": "BS",
        "cities": [
          "Sohar",
          "Saham",
          "Shinas"
        ]
      }
    ]
  },
  {
    "name": "Países Baixos",
    "code": "NL",
    "states": [
      {
        "name": "Holanda do Norte",
        "code": "NH",
        "cities": [
          "Amsterdã",
          "Haarlem",
          "Zaanstad",
          "Haarlemmermeer",
          "Alkmaar",
          "Amstelveen",
          "Hilversum",
          "Purmerend",
          "Hoorn"
        ]
      },
      {
        "name": "Holanda do Sul",
        "code": "ZH",
        "cities": [
          "Roterdã",
          "Haia (Den Haag)",
          "Zoetermeer",
          "Leiden",
          "Dordrecht",
          "Alphen aan den Rijn",
          "Westland",
          "Delft",
          "Schiedam",
          "Gouda"
        ]
      },
      {
        "name": "Utrecht",
        "code": "UT",
        "cities": [
          "Utrecht",
          "Amersfoort",
          "Veenendaal",
          "Zeist",
          "Nieuwegein"
        ]
      },
      {
        "name": "Brabante do Norte",
        "code": "NB",
        "cities": [
          "Eindhoven",
          "Tilburg",
          "Breda",
          "'s-Hertogenbosch",
          "Helmond",
          "Oss",
          "Roosendaal",
          "Bergen op Zoom"
        ]
      },
      {
        "name": "Guéldria",
        "code": "GE",
        "cities": [
          "Nijmegen",
          "Apeldoorn",
          "Arnhem",
          "Ede",
          "Doetinchem",
          "Barneveld"
        ]
      },
      {
        "name": "Overissel",
        "code": "OV",
        "cities": [
          "Enschede",
          "Zwolle",
          "Deventer",
          "Hengelo",
          "Almelo"
        ]
      },
      {
        "name": "Groninga",
        "code": "GR",
        "cities": [
          "Groninga",
          "Hoogezand-Sappemeer",
          "Stadskanaal"
        ]
      },
      {
        "name": "Limburgo",
        "code": "LI",
        "cities": [
          "Maastricht",
          "Venlo",
          "Sittard-Geleen",
          "Heerlen",
          "Roermond"
        ]
      },
      {
        "name": "Frísia",
        "code": "FR",
        "cities": [
          "Leeuwarden",
          "Drachten",
          "Sneek",
          "Heerenveen"
        ]
      },
      {
        "name": "Flevolândia",
        "code": "FL",
        "cities": [
          "Almere",
          "Lelystad",
          "Dronten"
        ]
      },
      {
        "name": "Drente",
        "code": "DR",
        "cities": [
          "Emmen",
          "Assen",
          "Hoogeveen"
        ]
      },
      {
        "name": "Zelândia",
        "code": "ZE",
        "cities": [
          "Middelburg",
          "Vlissingen",
          "Goes",
          "Terneuzen"
        ]
      }
    ]
  },
  {
    "name": "Palau",
    "code": "PW",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Ngerulmud",
          "Koror",
          "Airai",
          "Kloulklubed"
        ]
      }
    ]
  },
  {
    "name": "Palestina",
    "code": "PS",
    "states": [
      {
        "name": "Cisjordânia / Gaza",
        "code": "PS",
        "cities": [
          "Ramallah",
          "Gaza",
          "Hebron",
          "Nablus",
          "Belém",
          "Jenin",
          "Jericó"
        ]
      }
    ]
  },
  {
    "name": "Panamá",
    "code": "PA",
    "states": [
      {
        "name": "Panamá",
        "code": "8",
        "cities": [
          "Cidade do Panamá",
          "San Miguelito",
          "Tocumen",
          "Las Cumbres",
          "Pacora",
          "Chepo"
        ]
      },
      {
        "name": "Panamá Oeste",
        "code": "10",
        "cities": [
          "Arraiján",
          "La Chorrera",
          "Capira",
          "Chame"
        ]
      },
      {
        "name": "Chiriquí",
        "code": "4",
        "cities": [
          "David",
          "Boquete",
          "Bugaba",
          "Barú"
        ]
      },
      {
        "name": "Colón",
        "code": "3",
        "cities": [
          "Colón",
          "Sabanitas",
          "Cativá"
        ]
      }
    ]
  },
  {
    "name": "Papua-Nova Guiné",
    "code": "PG",
    "states": [
      {
        "name": "Papua-Nova Guiné",
        "code": "PG",
        "cities": [
          "Port Moresby",
          "Lae",
          "Arawa",
          "Mt Hagen",
          "Madang",
          "Goroka"
        ]
      }
    ]
  },
  {
    "name": "Paquistão",
    "code": "PK",
    "states": [
      {
        "name": "Punjab",
        "code": "PB",
        "cities": [
          "Lahore",
          "Faisalabad",
          "Rawalpindi",
          "Gujranwala",
          "Multan",
          "Sialkot",
          "Bahawalpur",
          "Sargodha",
          "Sheikhupura"
        ]
      },
      {
        "name": "Sindh",
        "code": "SD",
        "cities": [
          "Karachi",
          "Hyderabad",
          "Sukkur",
          "Larkana",
          "Nawabshah",
          "Mirpur Khas"
        ]
      },
      {
        "name": "Khyber Pakhtunkhwa",
        "code": "KP",
        "cities": [
          "Peshawar",
          "Mardan",
          "Abbottabad",
          "Swat (Mingora)",
          "Kohat"
        ]
      },
      {
        "name": "Território da Capital",
        "code": "IS",
        "cities": [
          "Islamabad"
        ]
      },
      {
        "name": "Baluchistão",
        "code": "BA",
        "cities": [
          "Quetta",
          "Turbat",
          "Gwadar",
          "Khuzdar",
          "Hub"
        ]
      }
    ]
  },
  {
    "name": "Paraguai",
    "code": "PY",
    "states": [
      {
        "name": "Asunción",
        "code": "ASU",
        "cities": [
          "Asunción"
        ]
      },
      {
        "name": "Central",
        "code": "11",
        "cities": [
          "Luque",
          "San Lorenzo",
          "Capiatá",
          "Lambaré",
          "Fernando de la Mora",
          "Limpio",
          "Ñemby",
          "Mariano Roque Alonso",
          "Villa Elisa",
          "Itauguá",
          "Areguá"
        ]
      },
      {
        "name": "Alto Paraná",
        "code": "10",
        "cities": [
          "Ciudad del Este",
          "Hernandarias",
          "Presidente Franco",
          "Minga Guazú",
          "Santa Rita"
        ]
      },
      {
        "name": "Itapúa",
        "code": "07",
        "cities": [
          "Encarnación",
          "Cambyretá",
          "Hohenau",
          "Bella Vista",
          "Obligado"
        ]
      },
      {
        "name": "Caaguazú",
        "code": "05",
        "cities": [
          "Coronel Oviedo",
          "Caaguazú",
          "J. Eulogio Estigarribia"
        ]
      },
      {
        "name": "Cordillera",
        "code": "03",
        "cities": [
          "Caacupé",
          "Tobatí",
          "Piribebuy",
          "San Bernardino"
        ]
      },
      {
        "name": "Concepción",
        "code": "01",
        "cities": [
          "Concepción",
          "Horqueta"
        ]
      },
      {
        "name": "Guairá",
        "code": "04",
        "cities": [
          "Villarrica",
          "Colonia Independencia"
        ]
      },
      {
        "name": "Paraguarí",
        "code": "09",
        "cities": [
          "Paraguarí",
          "Carapeguá",
          "Yaguarón"
        ]
      },
      {
        "name": "Amambay",
        "code": "13",
        "cities": [
          "Pedro Juan Caballero",
          "Capitán Bado"
        ]
      }
    ]
  },
  {
    "name": "Peru",
    "code": "PE",
    "states": [
      {
        "name": "Lima",
        "code": "LIM",
        "cities": [
          "Lima",
          "Callao",
          "Miraflores",
          "San Isidro",
          "Surco",
          "La Molina",
          "Los Olivos",
          "San Juan de Lurigancho",
          "Huacho",
          "Cañete"
        ]
      },
      {
        "name": "Arequipa",
        "code": "ARE",
        "cities": [
          "Arequipa",
          "Cayma",
          "Cerro Colorado",
          "Yanahuara",
          "Camaná",
          "Mollendo"
        ]
      },
      {
        "name": "La Libertad",
        "code": "LAL",
        "cities": [
          "Trujillo",
          "Huanchaco",
          "Victor Larco Herrera",
          "Chepén",
          "Pacasmayo"
        ]
      },
      {
        "name": "Cusco",
        "code": "CUS",
        "cities": [
          "Cusco",
          "Wanchaq",
          "Santiago",
          "Sicuani",
          "Urubamba",
          "Ollantaytambo"
        ]
      },
      {
        "name": "Lambayeque",
        "code": "LAM",
        "cities": [
          "Chiclayo",
          "Lambayeque",
          "Ferreñafe",
          "Monsefú",
          "Pimentel"
        ]
      },
      {
        "name": "Piura",
        "code": "PIU",
        "cities": [
          "Piura",
          "Sullana",
          "Paita",
          "Talara",
          "Catacaos",
          "Máncora"
        ]
      },
      {
        "name": "Junín",
        "code": "JUN",
        "cities": [
          "Huancayo",
          "Tarma",
          "Jauja",
          "La Oroya",
          "Satipo"
        ]
      },
      {
        "name": "Áncash",
        "code": "ANC",
        "cities": [
          "Chimbote",
          "Huaraz",
          "Nuevo Chimbote",
          "Casma",
          "Huarmey"
        ]
      },
      {
        "name": "Ica",
        "code": "ICA",
        "cities": [
          "Ica",
          "Chincha Alta",
          "Pisco",
          "Nazca"
        ]
      },
      {
        "name": "Loreto",
        "code": "LOR",
        "cities": [
          "Iquitos",
          "Yurimaguas",
          "Nauta"
        ]
      },
      {
        "name": "San Martín",
        "code": "SAM",
        "cities": [
          "Tarapoto",
          "Moyobamba",
          "Juanjuí",
          "Rioja"
        ]
      },
      {
        "name": "Cajamarca",
        "code": "CAJ",
        "cities": [
          "Cajamarca",
          "Jaén",
          "Chota",
          "Celendín"
        ]
      },
      {
        "name": "Tacna",
        "code": "TAC",
        "cities": [
          "Tacna",
          "Ilo",
          "Moquegua"
        ]
      },
      {
        "name": "Puno",
        "code": "PUN",
        "cities": [
          "Puno",
          "Juliaca",
          "Ayaviri"
        ]
      },
      {
        "name": "Ucayali",
        "code": "UCA",
        "cities": [
          "Pucallpa",
          "Coronel Portillo",
          "Yarinacocha"
        ]
      },
      {
        "name": "Huánuco",
        "code": "HUC",
        "cities": [
          "Huánuco",
          "Tingo María"
        ]
      },
      {
        "name": "Ayacucho",
        "code": "AYA",
        "cities": [
          "Ayacucho",
          "Huanta"
        ]
      }
    ]
  },
  {
    "name": "Polinésia Francesa",
    "code": "PF",
    "states": [
      {
        "name": "Polinésia Francesa",
        "code": "PF",
        "cities": [
          "Papeete",
          "Faaa",
          "Punaauia",
          "Pirae",
          "Bora Bora"
        ]
      }
    ]
  },
  {
    "name": "Polônia",
    "code": "PL",
    "states": [
      {
        "name": "Mazóvia",
        "code": "14",
        "cities": [
          "Varsóvia",
          "Radom",
          "Płock",
          "Siedlce",
          "Pruszków",
          "Legionowo",
          "Ostrołęka",
          "Piaseczno",
          "Otwock",
          "Ciechanów"
        ]
      },
      {
        "name": "Pequena Polônia",
        "code": "12",
        "cities": [
          "Cracóvia",
          "Tarnów",
          "Nowy Sącz",
          "Oświęcim",
          "Chrzanów",
          "Olkusz",
          "Nowy Targ",
          "Bochnia",
          "Zakopane"
        ]
      },
      {
        "name": "Silésia",
        "code": "24",
        "cities": [
          "Katowice",
          "Gliwice",
          "Sosnowiec",
          "Zabrze",
          "Bielsko-Biała",
          "Bytom",
          "Ruda Śląska",
          "Rybnik",
          "Tychy",
          "Dąbrowa Górnicza",
          "Chorzów",
          "Jaworzno",
          "Jastrzębie-Zdrój",
          "Mysłowice",
          "Siemianowice Śląskie"
        ]
      },
      {
        "name": "Baixa Silésia",
        "code": "02",
        "cities": [
          "Breslávia (Wrocław)",
          "Wałbrzych",
          "Legnica",
          "Jelenia Góra",
          "Lubin",
          "Głogów",
          "Świdnica",
          "Bolesławiec"
        ]
      },
      {
        "name": "Grande Polônia",
        "code": "30",
        "cities": [
          "Poznań",
          "Kalisz",
          "Konin",
          "Piła",
          "Ostrów Wielkopolski",
          "Gniezno",
          "Leszno",
          "Swarzędz"
        ]
      },
      {
        "name": "Pomerânia",
        "code": "22",
        "cities": [
          "Gdańsk",
          "Gdynia",
          "Słupsk",
          "Tczew",
          "Wejherowo",
          "Starogard Gdański",
          "Sopot",
          "Rumia",
          "Malbork"
        ]
      },
      {
        "name": "Łódź",
        "code": "10",
        "cities": [
          "Łódź",
          "Piotrków Trybunalski",
          "Pabianice",
          "Tomaszów Mazowiecki",
          "Bełchatów",
          "Zgierz",
          "Skierniewice",
          "Radomsko"
        ]
      },
      {
        "name": "Cujávia-Pomerânia",
        "code": "04",
        "cities": [
          "Bydgoszcz",
          "Toruń",
          "Włocławek",
          "Grudziądz",
          "Inowrocław",
          "Brodnica"
        ]
      },
      {
        "name": "Lublin",
        "code": "06",
        "cities": [
          "Lublin",
          "Zamość",
          "Chełm",
          "Biała Podlaska",
          "Puławy",
          "Świdnik"
        ]
      },
      {
        "name": "Pomerânia Ocidental",
        "code": "32",
        "cities": [
          "Szczecin",
          "Koszalin",
          "Stargard",
          "Kołobrzeg",
          "Świnoujście",
          "Szczecinek"
        ]
      },
      {
        "name": "Podcarpátia",
        "code": "18",
        "cities": [
          "Rzeszów",
          "Przemyśl",
          "Stalowa Wola",
          "Mielec",
          "Tarnobrzeg",
          "Krosno"
        ]
      },
      {
        "name": "Vármia-Masúria",
        "code": "28",
        "cities": [
          "Olsztyn",
          "Elbląg",
          "Ełk",
          "Iława",
          "Ostróda",
          "Giżycko"
        ]
      },
      {
        "name": "Santa Cruz (Świętokrzyskie)",
        "code": "26",
        "cities": [
          "Kielce",
          "Ostrowiec Świętokrzyski",
          "Starachowice",
          "Skarżysko-Kamienna",
          "Sandomierz"
        ]
      },
      {
        "name": "Podláquia",
        "code": "20",
        "cities": [
          "Białystok",
          "Suwałki",
          "Łomża",
          "Augustów"
        ]
      },
      {
        "name": "Lubúsquia",
        "code": "08",
        "cities": [
          "Gorzów Wielkopolski",
          "Zielona Góra",
          "Nowa Sól",
          "Żary"
        ]
      },
      {
        "name": "Opole",
        "code": "16",
        "cities": [
          "Opole",
          "Kędzierzyn-Koźle",
          "Nysa",
          "Brzeg",
          "Kluczbork"
        ]
      }
    ]
  },
  {
    "name": "Porto Rico",
    "code": "PR",
    "states": [
      {
        "name": "Porto Rico",
        "code": "PR",
        "cities": [
          "San Juan",
          "Bayamón",
          "Carolina",
          "Ponce",
          "Caguas",
          "Guaynabo",
          "Mayagüez",
          "Arecibo",
          "Trujillo Alto"
        ]
      }
    ]
  },
  {
    "name": "Portugal",
    "code": "PT",
    "states": [
      {
        "name": "Lisboa",
        "code": "11",
        "cities": [
          "Lisboa",
          "Sintra",
          "Cascais",
          "Loures",
          "Amadora",
          "Oeiras",
          "Odivelas",
          "Vila Franca de Xira",
          "Mafra",
          "Torres Vedras",
          "Alenquer",
          "Lourinhã",
          "Arruda dos Vinhos"
        ]
      },
      {
        "name": "Porto",
        "code": "13",
        "cities": [
          "Porto",
          "Vila Nova de Gaia",
          "Matosinhos",
          "Maia",
          "Gondomar",
          "Póvoa de Varzim",
          "Vila do Conde",
          "Valongo",
          "Santo Tirso",
          "Penafiel",
          "Amarante",
          "Marco de Canaveses",
          "Felgueiras",
          "Trofa"
        ]
      },
      {
        "name": "Braga",
        "code": "03",
        "cities": [
          "Braga",
          "Guimarães",
          "Vila Nova de Famalicão",
          "Barcelos",
          "Fafe",
          "Vila Verde",
          "Esposende",
          "Vizela",
          "Póvoa de Lanhoso",
          "Amares",
          "Cabeceiras de Basto"
        ]
      },
      {
        "name": "Setúbal",
        "code": "15",
        "cities": [
          "Setúbal",
          "Almada",
          "Seixal",
          "Barreiro",
          "Montijo",
          "Sesimbra",
          "Palmela",
          "Moita",
          "Alcochete",
          "Santiago do Cacém",
          "Sines",
          "Grândola"
        ]
      },
      {
        "name": "Aveiro",
        "code": "01",
        "cities": [
          "Aveiro",
          "Santa Maria da Feira",
          "Oliveira de Azeméis",
          "Ovar",
          "Águeda",
          "Ílhavo",
          "São João da Madeira",
          "Espinho",
          "Estarreja",
          "Albergaria-a-Velha",
          "Anadia"
        ]
      },
      {
        "name": "Leiria",
        "code": "10",
        "cities": [
          "Leiria",
          "Caldas da Rainha",
          "Marinha Grande",
          "Pombal",
          "Alcobaça",
          "Peniche",
          "Porto de Mós",
          "Batalha",
          "Nazaré"
        ]
      },
      {
        "name": "Coimbra",
        "code": "06",
        "cities": [
          "Coimbra",
          "Figueira da Foz",
          "Cantanhede",
          "Montemor-o-Velho",
          "Lousã",
          "Condeixa-a-Nova",
          "Soure",
          "Oliveira do Hospital"
        ]
      },
      {
        "name": "Faro (Algarve)",
        "code": "08",
        "cities": [
          "Faro",
          "Portimão",
          "Loulé",
          "Albufeira",
          "Olhão",
          "Silves",
          "Lagos",
          "Tavira",
          "Lagoa",
          "Vila Real de Santo António",
          "São Brás de Alportel"
        ]
      },
      {
        "name": "Viseu",
        "code": "18",
        "cities": [
          "Viseu",
          "Lamego",
          "Mangualde",
          "Tondela",
          "São Pedro do Sul",
          "Cinfães",
          "Castro Daire"
        ]
      },
      {
        "name": "Viana do Castelo",
        "code": "16",
        "cities": [
          "Viana do Castelo",
          "Ponte de Lima",
          "Arcos de Valdevez",
          "Caminha",
          "Valença",
          "Monção"
        ]
      },
      {
        "name": "Santarém",
        "code": "14",
        "cities": [
          "Santarém",
          "Tomar",
          "Torres Novas",
          "Abrantes",
          "Ourém",
          "Entroncamento",
          "Rio Maior",
          "Cartaxo",
          "Benavente"
        ]
      },
      {
        "name": "Castelo Branco",
        "code": "05",
        "cities": [
          "Castelo Branco",
          "Covilhã",
          "Fundão",
          "Sertã"
        ]
      },
      {
        "name": "Vila Real",
        "code": "17",
        "cities": [
          "Vila Real",
          "Chaves",
          "Peso da Régua",
          "Valpaços"
        ]
      },
      {
        "name": "Évora",
        "code": "07",
        "cities": [
          "Évora",
          "Montemor-o-Novo",
          "Vendas Novas",
          "Estremoz",
          "Reguengos de Monsaraz"
        ]
      },
      {
        "name": "Guarda",
        "code": "09",
        "cities": [
          "Guarda",
          "Seia",
          "Gouveia",
          "Pinhel"
        ]
      },
      {
        "name": "Beja",
        "code": "02",
        "cities": [
          "Beja",
          "Serpa",
          "Moura",
          "Odemira"
        ]
      },
      {
        "name": "Bragança",
        "code": "04",
        "cities": [
          "Bragança",
          "Mirandela",
          "Macedo de Cavaleiros"
        ]
      },
      {
        "name": "Portalegre",
        "code": "12",
        "cities": [
          "Portalegre",
          "Elvas",
          "Ponte de Sor"
        ]
      },
      {
        "name": "Madeira",
        "code": "30",
        "cities": [
          "Funchal",
          "Santa Cruz",
          "Câmara de Lobos",
          "Machico",
          "Ribeira Brava",
          "Calheta"
        ]
      },
      {
        "name": "Açores",
        "code": "20",
        "cities": [
          "Ponta Delgada",
          "Angra do Heroísmo",
          "Ribeira Grande",
          "Praia da Vitória",
          "Horta",
          "Lagoa"
        ]
      }
    ]
  },
  {
    "name": "Quênia",
    "code": "KE",
    "states": [
      {
        "name": "Nairobi",
        "code": "30",
        "cities": [
          "Nairobi",
          "Westlands",
          "Kibera",
          "Kilimani",
          "Karen",
          "Lang'ata",
          "Embakasi",
          "Kasaran"
        ]
      },
      {
        "name": "Mombasa",
        "code": "28",
        "cities": [
          "Mombasa",
          "Nyali",
          "Changamwe",
          "Likoni",
          "Kisauni"
        ]
      },
      {
        "name": "Kisumu",
        "code": "20",
        "cities": [
          "Kisumu",
          "Muhoroni"
        ]
      },
      {
        "name": "Nakuru",
        "code": "31",
        "cities": [
          "Nakuru",
          "Naivasha",
          "Molo"
        ]
      },
      {
        "name": "Uasin Gishu",
        "code": "44",
        "cities": [
          "Eldoret"
        ]
      },
      {
        "name": "Kiambu",
        "code": "13",
        "cities": [
          "Thika",
          "Ruiru",
          "Kikuyu",
          "Kiambu"
        ]
      }
    ]
  },
  {
    "name": "Quirguistão",
    "code": "KG",
    "states": [
      {
        "name": "Bishkek",
        "code": "GB",
        "cities": [
          "Bishkek",
          "Osh",
          "Jalal-Abad",
          "Karakol",
          "Tokmok"
        ]
      }
    ]
  },
  {
    "name": "Quiribati",
    "code": "KI",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Tarawa do Sul",
          "Betio",
          "Bikenibeu",
          "Teaoraereke"
        ]
      }
    ]
  },
  {
    "name": "Reino Unido",
    "code": "UK",
    "states": [
      {
        "name": "Inglaterra",
        "code": "ENG",
        "cities": [
          "Londres",
          "Manchester",
          "Birmingham",
          "Liverpool",
          "Leeds",
          "Sheffield",
          "Bristol",
          "Newcastle upon Tyne",
          "Nottingham",
          "Leicester",
          "Southampton",
          "Portsmouth",
          "Oxford",
          "Cambridge",
          "Brighton",
          "Reading",
          "Plymouth",
          "Derby",
          "Stoke-on-Trent",
          "Coventry",
          "Milton Keynes",
          "Norwich",
          "Bournemouth",
          "Middlesbrough",
          "Blackpool",
          "York",
          "Bath",
          "Exeter"
        ]
      },
      {
        "name": "Escócia",
        "code": "SCT",
        "cities": [
          "Edimburgo",
          "Glasgow",
          "Aberdeen",
          "Dundee",
          "Inverness",
          "Stirling",
          "Perth",
          "Paisley",
          "East Kilbride",
          "Livingston",
          "Dunfermline"
        ]
      },
      {
        "name": "País de Gales",
        "code": "WLS",
        "cities": [
          "Cardiff",
          "Swansea",
          "Newport",
          "Wrexham",
          "Barry",
          "Neath",
          "Bridgend",
          "Cwmbran"
        ]
      },
      {
        "name": "Irlanda do Norte",
        "code": "NIR",
        "cities": [
          "Belfast",
          "Derry (Londonderry)",
          "Lisburn",
          "Newry",
          "Bangor",
          "Craigavon",
          "Ballymena",
          "Newtownards"
        ]
      }
    ]
  },
  {
    "name": "República Centro-Africana",
    "code": "CF",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Bangui",
          "Bimbo",
          "Berbérati",
          "Carnot",
          "Bambari"
        ]
      }
    ]
  },
  {
    "name": "República Dominicana",
    "code": "DO",
    "states": [
      {
        "name": "Distrito Nacional",
        "code": "01",
        "cities": [
          "Santo Domingo"
        ]
      },
      {
        "name": "Santiago",
        "code": "25",
        "cities": [
          "Santiago de los Caballeros",
          "Villa Bisonó",
          "Tamboril"
        ]
      },
      {
        "name": "Santo Domingo",
        "code": "32",
        "cities": [
          "Santo Domingo Este",
          "Santo Domingo Norte",
          "Santo Domingo Oeste",
          "Boca Chica",
          "Los Alcarrizos"
        ]
      },
      {
        "name": "La Altagracia",
        "code": "11",
        "cities": [
          "Higüey",
          "Punta Cana",
          "Bávaro"
        ]
      },
      {
        "name": "Puerto Plata",
        "code": "18",
        "cities": [
          "Puerto Plata",
          "Sosúa",
          "Cabarete"
        ]
      }
    ]
  },
  {
    "name": "República Tcheca",
    "code": "CZ",
    "states": [
      {
        "name": "Praga",
        "code": "10",
        "cities": [
          "Praga"
        ]
      },
      {
        "name": "Morávia do Sul",
        "code": "64",
        "cities": [
          "Brno",
          "Znojmo",
          "Hodonín",
          "Břeclav",
          "Vyškov"
        ]
      },
      {
        "name": "Morávia-Silésia",
        "code": "80",
        "cities": [
          "Ostrava",
          "Havířov",
          "Opava",
          "Frýdek-Místek",
          "Karviná",
          "Třinec"
        ]
      },
      {
        "name": "Boêmia Central",
        "code": "20",
        "cities": [
          "Kladno",
          "Mladá Boleslav",
          "Příbram",
          "Kolín",
          "Kutná Hora",
          "Mělník",
          "Beroun"
        ]
      },
      {
        "name": "Plzeň",
        "code": "32",
        "cities": [
          "Plzeň",
          "Klatovy",
          "Rokycany",
          "Tachov",
          "Domažlice"
        ]
      },
      {
        "name": "Ústí nad Labem",
        "code": "42",
        "cities": [
          "Ústí nad Labem",
          "Most",
          "Děčín",
          "Teplice",
          "Chomutov",
          "Litoměřice"
        ]
      },
      {
        "name": "Olomouc",
        "code": "71",
        "cities": [
          "Olomouc",
          "Prostějov",
          "Přerov",
          "Šumperk",
          "Hranice"
        ]
      },
      {
        "name": "Hradec Králové",
        "code": "52",
        "cities": [
          "Hradec Králové",
          "Trutnov",
          "Náchod",
          "Jičín",
          "Rychnov nad Kněžnou"
        ]
      },
      {
        "name": "Pardubice",
        "code": "53",
        "cities": [
          "Pardubice",
          "Chrudim",
          "Svitavy",
          "Ústí nad Orlicí"
        ]
      },
      {
        "name": "Zlín",
        "code": "72",
        "cities": [
          "Zlín",
          "Kroměříž",
          "Vsetín",
          "Uherské Hradiště",
          "Valašské Meziříčí"
        ]
      },
      {
        "name": "Boêmia do Sul",
        "code": "31",
        "cities": [
          "České Budějovice",
          "Tábor",
          "Písek",
          "Strakonice",
          "Jindřichův Hradec"
        ]
      },
      {
        "name": "Liberec",
        "code": "51",
        "cities": [
          "Liberec",
          "Jablonec nad Nisou",
          "Česká Lípa",
          "Turnov"
        ]
      },
      {
        "name": "Vysočina",
        "code": "63",
        "cities": [
          "Jihlava",
          "Třebíč",
          "Havlíčkův Brod",
          "Žďár nad Sázavou",
          "Pelhřimov"
        ]
      },
      {
        "name": "Karlovy Vary",
        "code": "41",
        "cities": [
          "Karlovy Vary",
          "Cheb",
          "Sokolov",
          "Ostrov"
        ]
      }
    ]
  },
  {
    "name": "Romênia",
    "code": "RO",
    "states": [
      {
        "name": "Bucareste",
        "code": "B",
        "cities": [
          "Bucareste",
          "Voluntari",
          "Pantelimon",
          "Popești-Leordeni"
        ]
      },
      {
        "name": "Cluj",
        "code": "CJ",
        "cities": [
          "Cluj-Napoca",
          "Turda",
          "Dej",
          "Câmpia Turzii"
        ]
      },
      {
        "name": "Timiș",
        "code": "TM",
        "cities": [
          "Timișoara",
          "Lugoj",
          "Sânnicolau Mare"
        ]
      },
      {
        "name": "Iași",
        "code": "IS",
        "cities": [
          "Iași",
          "Pașcani",
          "Hârlău"
        ]
      },
      {
        "name": "Constanța",
        "code": "CT",
        "cities": [
          "Constanța",
          "Medgidia",
          "Mangalia",
          "Năvodari"
        ]
      },
      {
        "name": "Brașov",
        "code": "BV",
        "cities": [
          "Brașov",
          "Făgăraș",
          "Săcele",
          "Codlea"
        ]
      },
      {
        "name": "Prahova",
        "code": "PH",
        "cities": [
          "Ploiești",
          "Câmpina",
          "Băicoi",
          "Sinaia"
        ]
      },
      {
        "name": "Dolj",
        "code": "DJ",
        "cities": [
          "Craiova",
          "Băilești",
          "Calafat"
        ]
      },
      {
        "name": "Galați",
        "code": "GL",
        "cities": [
          "Galați",
          "Tecuci"
        ]
      },
      {
        "name": "Bihor",
        "code": "BH",
        "cities": [
          "Oradea",
          "Salonta",
          "Marghita"
        ]
      },
      {
        "name": "Sibiu",
        "code": "SB",
        "cities": [
          "Sibiu",
          "Mediaș",
          "Cisnădie"
        ]
      },
      {
        "name": "Argeș",
        "code": "AG",
        "cities": [
          "Pitești",
          "Mioveni",
          "Câmpulung",
          "Curtea de Argeș"
        ]
      },
      {
        "name": "Mureș",
        "code": "MS",
        "cities": [
          "Târgu Mureș",
          "Reghin",
          "Sighișoara"
        ]
      },
      {
        "name": "Bacău",
        "code": "BC",
        "cities": [
          "Bacău",
          "Onești",
          "Moinești"
        ]
      },
      {
        "name": "Arad",
        "code": "AR",
        "cities": [
          "Arad",
          "Lipova",
          "Ineu"
        ]
      }
    ]
  },
  {
    "name": "Ruanda",
    "code": "RW",
    "states": [
      {
        "name": "Kigali",
        "code": "01",
        "cities": [
          "Kigali",
          "Gasabo",
          "Kicukiro",
          "Nyarugenge"
        ]
      },
      {
        "name": "Províncias",
        "code": "02",
        "cities": [
          "Butare (Huye)",
          "Gisenyi (Rubavu)",
          "Ruhengeri (Musanze)",
          "Gitarama (Muhanga)",
          "Kibuye (Karongi)"
        ]
      }
    ]
  },
  {
    "name": "Rússia",
    "code": "RU",
    "states": [
      {
        "name": "Moscou (Cidade)",
        "code": "MOW",
        "cities": [
          "Moscou",
          "Zelenograd"
        ]
      },
      {
        "name": "São Petersburgo (Cidade)",
        "code": "SPE",
        "cities": [
          "São Petersburgo",
          "Kolpino",
          "Pushkin",
          "Petergof"
        ]
      },
      {
        "name": "Oblast de Moscou",
        "code": "MOS",
        "cities": [
          "Balashikha",
          "Podolsk",
          "Khimki",
          "Mytishchi",
          "Korolyov",
          "Lyubertsy",
          "Krasnogorsk",
          "Elektrostal",
          "Kolomna",
          "Odintsovo",
          "Serpukhov"
        ]
      },
      {
        "name": "Sverdlovsk (Urais)",
        "code": "SVE",
        "cities": [
          "Ecaterimburgo",
          "Nizhny Tagil",
          "Kamensk-Uralsky",
          "Pervouralsk"
        ]
      },
      {
        "name": "Novosibirsk (Sibéria)",
        "code": "NVS",
        "cities": [
          "Novosibirsk",
          "Berdsk",
          "Iskitim"
        ]
      },
      {
        "name": "Tártaro (Tataristão)",
        "code": "TA",
        "cities": [
          "Cazã (Kazan)",
          "Naberezhnye Chelny",
          "Nizhnekamsk",
          "Almetyevsk"
        ]
      },
      {
        "name": "Nizhny Novgorod",
        "code": "NIZ",
        "cities": [
          "Nizhny Novgorod",
          "Dzerzhinsk",
          "Arzamas",
          "Sarov"
        ]
      },
      {
        "name": "Chelyabinsk",
        "code": "CHE",
        "cities": [
          "Chelyabinsk",
          "Magnitogorsk",
          "Zlatoust",
          "Miass",
          "Kopeysk"
        ]
      },
      {
        "name": "Samara",
        "code": "SAM",
        "cities": [
          "Samara",
          "Tolyatti",
          "Syzran",
          "Novokuybyshevsk"
        ]
      },
      {
        "name": "Rostov",
        "code": "ROS",
        "cities": [
          "Rostov-on-Don",
          "Taganrog",
          "Shakhty",
          "Novocherkassk",
          "Volgodonsk",
          "Bataysk"
        ]
      },
      {
        "name": "Bashkortostan",
        "code": "BA",
        "cities": [
          "Ufa",
          "Sterlitamak",
          "Salavat",
          "Neftekamsk",
          "Oktyabrsky"
        ]
      },
      {
        "name": "Krasnoyarsk",
        "code": "KYA",
        "cities": [
          "Krasnoyarsk",
          "Norilsk",
          "Achinsk",
          "Kansk",
          "Zheleznogorsk"
        ]
      },
      {
        "name": "Krasnodar (Kuban)",
        "code": "KDA",
        "cities": [
          "Krasnodar",
          "Sochi",
          "Novorossiysk",
          "Armavir",
          "Yeysk",
          "Anapa"
        ]
      },
      {
        "name": "Perm",
        "code": "PER",
        "cities": [
          "Perm",
          "Berezniki",
          "Solikamsk",
          "Chaykovsky"
        ]
      },
      {
        "name": "Voronezh",
        "code": "VOR",
        "cities": [
          "Voronezh",
          "Rossosh",
          "Borisoglebsk"
        ]
      },
      {
        "name": "Volgogrado",
        "code": "VGG",
        "cities": [
          "Volgogrado",
          "Volzhsky",
          "Kamyshin"
        ]
      },
      {
        "name": "Primorsky (Extremo Oriente)",
        "code": "PRI",
        "cities": [
          "Vladivostok",
          "Ussuriysk",
          "Nakhodka",
          "Artyom"
        ]
      },
      {
        "name": "Omsk",
        "code": "OMS",
        "cities": [
          "Omsk",
          "Tara",
          "Isilkul"
        ]
      },
      {
        "name": "Irkutsk",
        "code": "IRK",
        "cities": [
          "Irkutsk",
          "Bratsk",
          "Angarsk",
          "Ust-Ilimsk"
        ]
      },
      {
        "name": "Khabarovsk",
        "code": "KHA",
        "cities": [
          "Khabarovsk",
          "Komsomolsk-on-Amur",
          "Amursk"
        ]
      },
      {
        "name": "Kaliningrado",
        "code": "KGD",
        "cities": [
          "Kaliningrado",
          "Sovetsk",
          "Chernyakhovsk",
          "Baltiysk"
        ]
      }
    ]
  },
  {
    "name": "Samoa",
    "code": "WS",
    "states": [
      {
        "name": "Samoa",
        "code": "WS",
        "cities": [
          "Apia",
          "Vaitele",
          "Faleasiu",
          "Vailele"
        ]
      }
    ]
  },
  {
    "name": "San Marino",
    "code": "SM",
    "states": [
      {
        "name": "San Marino",
        "code": "SM",
        "cities": [
          "Cidade de San Marino",
          "Serravalle",
          "Borgo Maggiore",
          "Domagnano"
        ]
      }
    ]
  },
  {
    "name": "Santa Lúcia",
    "code": "LC",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Castries",
          "Vieux Fort",
          "Gros Islet",
          "Soufrière"
        ]
      }
    ]
  },
  {
    "name": "São Cristóvão e Névis",
    "code": "KN",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Basseterre",
          "Charlestown",
          "Sandy Point Town",
          "Cayon"
        ]
      }
    ]
  },
  {
    "name": "São Tomé e Príncipe",
    "code": "ST",
    "states": [
      {
        "name": "São Tomé",
        "code": "ST",
        "cities": [
          "São Tomé",
          "Trindade",
          "Santana",
          "Neves",
          "Guadalupe"
        ]
      },
      {
        "name": "Príncipe",
        "code": "PR",
        "cities": [
          "Santo António"
        ]
      }
    ]
  },
  {
    "name": "São Vicente e Granadinas",
    "code": "VC",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Kingstown",
          "Georgetown",
          "Barrouallie",
          "Port Elizabeth"
        ]
      }
    ]
  },
  {
    "name": "Seicheles",
    "code": "SC",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Victoria",
          "Anse Etoile",
          "Beau Vallon",
          "Cascade"
        ]
      }
    ]
  },
  {
    "name": "Senegal",
    "code": "SN",
    "states": [
      {
        "name": "Dakar",
        "code": "DK",
        "cities": [
          "Dakar",
          "Pikine",
          "Guédiawaye",
          "Rufisque",
          "Almadies",
          "Ngor"
        ]
      },
      {
        "name": "Thiès",
        "code": "TH",
        "cities": [
          "Thiès",
          "M'bour",
          "Tivaouane",
          "Saly"
        ]
      },
      {
        "name": "Saint-Louis",
        "code": "SL",
        "cities": [
          "Saint-Louis",
          "Richard Toll",
          "Dagana"
        ]
      },
      {
        "name": "Ziguinchor",
        "code": "ZG",
        "cities": [
          "Ziguinchor",
          "Bignona",
          "Cap Skirring"
        ]
      }
    ]
  },
  {
    "name": "Serra Leoa",
    "code": "SL",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Freetown",
          "Kenema",
          "Bo",
          "Koidu",
          "Makeni"
        ]
      }
    ]
  },
  {
    "name": "Sérvia",
    "code": "RS",
    "states": [
      {
        "name": "Belgrado",
        "code": "00",
        "cities": [
          "Belgrado",
          "Zemun",
          "Novi Beograd",
          "Čukarica",
          "Voždovac"
        ]
      },
      {
        "name": "Voivodina",
        "code": "VO",
        "cities": [
          "Novi Sad",
          "Subotica",
          "Zrenjanin",
          "Pančevo",
          "Sombor",
          "Kikinda",
          "Sremska Mitrovica"
        ]
      },
      {
        "name": "Šumadija e Sérvia Ocidental",
        "code": "SW",
        "cities": [
          "Kragujevac",
          "Čačak",
          "Kraljevo",
          "Valjevo",
          "Šabac",
          "Užice",
          "Novi Pazar",
          "Kruševac"
        ]
      },
      {
        "name": "Sérvia Meridional e Oriental",
        "code": "SE",
        "cities": [
          "Niš",
          "Leskovac",
          "Vranje",
          "Pirot",
          "Zaječar",
          "Bor",
          "Prokuplje"
        ]
      }
    ]
  },
  {
    "name": "Singapura",
    "code": "SG",
    "states": [
      {
        "name": "Singapura",
        "code": "SG",
        "cities": [
          "Singapura",
          "Central",
          "Jurong East",
          "Woodlands",
          "Tampines",
          "Bedok",
          "Orchard",
          "Marina Bay",
          "Ang Mo Kio",
          "Yishun",
          "Punggol",
          "Sengkang",
          "Clementi",
          "Toa Payoh",
          "Bukit Batok",
          "Pasir Ris"
        ]
      }
    ]
  },
  {
    "name": "Síria",
    "code": "SY",
    "states": [
      {
        "name": "Damasco",
        "code": "DI",
        "cities": [
          "Damasco",
          "Alepo",
          "Homs",
          "Latakia",
          "Hama"
        ]
      }
    ]
  },
  {
    "name": "Somália",
    "code": "SO",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Mogadíscio",
          "Hargeisa",
          "Kismayo",
          "Berbera",
          "Merca",
          "Bosaso"
        ]
      }
    ]
  },
  {
    "name": "Sri Lanka",
    "code": "LK",
    "states": [
      {
        "name": "Província Ocidental",
        "code": "1",
        "cities": [
          "Colombo",
          "Dehiwala-Mount Lavinia",
          "Moratuwa",
          "Sri Jayawardenepura Kotte",
          "Negombo"
        ]
      },
      {
        "name": "Província Central",
        "code": "2",
        "cities": [
          "Kandy",
          "Matale",
          "Nuwara Eliya"
        ]
      },
      {
        "name": "Província do Sul",
        "code": "3",
        "cities": [
          "Galle",
          "Matara"
        ]
      }
    ]
  },
  {
    "name": "Sudão",
    "code": "SD",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Cartum",
          "Ondurmã",
          "Cartum Norte",
          "Porto Sudão",
          "Kassala",
          "El Obeid",
          "Nyala"
        ]
      }
    ]
  },
  {
    "name": "Sudão do Sul",
    "code": "SS",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Juba",
          "Wau",
          "Malakal",
          "Yei",
          "Yambio"
        ]
      }
    ]
  },
  {
    "name": "Suécia",
    "code": "SE",
    "states": [
      {
        "name": "Estocolmo",
        "code": "AB",
        "cities": [
          "Estocolmo",
          "Södertälje",
          "Täby",
          "Nacka",
          "Solna",
          "Järfälla",
          "Sollentuna",
          "Botkyrka",
          "Haninge",
          "Huddinge",
          "Sundbyberg"
        ]
      },
      {
        "name": "Västra Götaland",
        "code": "O",
        "cities": [
          "Gotemburgo",
          "Borås",
          "Trollhättan",
          "Skövde",
          "Uddevalla",
          "Alingsås",
          "Kungälv",
          "Vänersborg",
          "Mariestad"
        ]
      },
      {
        "name": "Escânia (Skåne)",
        "code": "M",
        "cities": [
          "Malmö",
          "Helsingborg",
          "Lund",
          "Kristianstad",
          "Landskrona",
          "Trelleborg",
          "Ängelholm",
          "Ystad",
          "Hässleholm"
        ]
      },
      {
        "name": "Uppsala",
        "code": "C",
        "cities": [
          "Uppsala",
          "Enköping",
          "Bålsta"
        ]
      },
      {
        "name": "Östergötland",
        "code": "E",
        "cities": [
          "Linköping",
          "Norrköping",
          "Motala",
          "Finspång"
        ]
      },
      {
        "name": "Jönköping",
        "code": "F",
        "cities": [
          "Jönköping",
          "Värnamo",
          "Nässjö",
          "Tranås",
          "Vetlanda"
        ]
      },
      {
        "name": "Västmanland",
        "code": "U",
        "cities": [
          "Västerås",
          "Köping",
          "Sala",
          "Fagersta"
        ]
      },
      {
        "name": "Örebro",
        "code": "T",
        "cities": [
          "Örebro",
          "Karlskoga",
          "Kumla",
          "Lindesberg"
        ]
      },
      {
        "name": "Värmland",
        "code": "S",
        "cities": [
          "Karlstad",
          "Kristinehamn",
          "Arvika",
          "Skoghall"
        ]
      },
      {
        "name": "Gävleborg",
        "code": "X",
        "cities": [
          "Gävle",
          "Sandviken",
          "Hudiksvall",
          "Bollnäs"
        ]
      },
      {
        "name": "Västerbotten",
        "code": "AC",
        "cities": [
          "Umeå",
          "Skellefteå",
          "Lycksele"
        ]
      },
      {
        "name": "Norrbotten",
        "code": "BD",
        "cities": [
          "Luleå",
          "Piteå",
          "Boden",
          "Kiruna",
          "Gällivare"
        ]
      }
    ]
  },
  {
    "name": "Suíça",
    "code": "CH",
    "states": [
      {
        "name": "Zurique",
        "code": "ZH",
        "cities": [
          "Zurique",
          "Winterthur",
          "Uster",
          "Dübendorf",
          "Dietikon"
        ]
      },
      {
        "name": "Genebra",
        "code": "GE",
        "cities": [
          "Genebra",
          "Vernier",
          "Lancy",
          "Meyrin",
          "Carouge"
        ]
      },
      {
        "name": "Vaud",
        "code": "VD",
        "cities": [
          "Lausanne",
          "Yverdon-les-Bains",
          "Montreux",
          "Renens",
          "Nyon",
          "Vevey"
        ]
      },
      {
        "name": "Berna",
        "code": "BE",
        "cities": [
          "Berna",
          "Biel/Bienne",
          "Thun",
          "Köniz",
          "Burgdorf"
        ]
      },
      {
        "name": "Basileia-Cidade",
        "code": "BS",
        "cities": [
          "Basileia",
          "Riehen"
        ]
      },
      {
        "name": "Lucerna",
        "code": "LU",
        "cities": [
          "Lucerna",
          "Emmen",
          "Kriens",
          "Horw"
        ]
      },
      {
        "name": "São Galo",
        "code": "SG",
        "cities": [
          "São Galo",
          "Rapperswil-Jona",
          "Wil",
          "Gossau"
        ]
      },
      {
        "name": "Ticino",
        "code": "TI",
        "cities": [
          "Lugano",
          "Bellinzona",
          "Locarno",
          "Mendrisio"
        ]
      },
      {
        "name": "Valais",
        "code": "VS",
        "cities": [
          "Sion",
          "Monthey",
          "Sierre",
          "Martigny",
          "Brig-Glis"
        ]
      },
      {
        "name": "Argóvia",
        "code": "AG",
        "cities": [
          "Aarau",
          "Baden",
          "Wettingen",
          "Wohlen"
        ]
      }
    ]
  },
  {
    "name": "Suriname",
    "code": "SR",
    "states": [
      {
        "name": "Paramaribo",
        "code": "PM",
        "cities": [
          "Paramaribo",
          "Lelydorp",
          "Nieuw Nickerie",
          "Moengo"
        ]
      }
    ]
  },
  {
    "name": "Tailândia",
    "code": "TH",
    "states": [
      {
        "name": "Bangkok",
        "code": "10",
        "cities": [
          "Bangkok",
          "Chatuchak",
          "Bang Rak",
          "Watthana",
          "Pathum Wan",
          "Khlong Toei"
        ]
      },
      {
        "name": "Chiang Mai",
        "code": "50",
        "cities": [
          "Chiang Mai",
          "Hang Dong",
          "San Sai"
        ]
      },
      {
        "name": "Chonburi",
        "code": "20",
        "cities": [
          "Pattaya",
          "Chonburi",
          "Si Racha",
          "Bang Lamung"
        ]
      },
      {
        "name": "Phuket",
        "code": "83",
        "cities": [
          "Phuket City",
          "Patong",
          "Kathu",
          "Thalang"
        ]
      },
      {
        "name": "Nonthaburi",
        "code": "12",
        "cities": [
          "Nonthaburi",
          "Pak Kret",
          "Bang Kruai"
        ]
      },
      {
        "name": "Samut Prakan",
        "code": "11",
        "cities": [
          "Samut Prakan",
          "Bang Phli",
          "Phra Pradaeng"
        ]
      },
      {
        "name": "Nakhon Ratchasima (Korat)",
        "code": "30",
        "cities": [
          "Nakhon Ratchasima",
          "Pak Chong"
        ]
      },
      {
        "name": "Songkhla",
        "code": "90",
        "cities": [
          "Hat Yai",
          "Songkhla"
        ]
      },
      {
        "name": "Khon Kaen",
        "code": "40",
        "cities": [
          "Khon Kaen",
          "Chum Phae"
        ]
      },
      {
        "name": "Udon Thani",
        "code": "41",
        "cities": [
          "Udon Thani"
        ]
      }
    ]
  },
  {
    "name": "Taiwan",
    "code": "TW",
    "states": [
      {
        "name": "Taipé",
        "code": "TPE",
        "cities": [
          "Taipé",
          "Xinyi",
          "Da'an",
          "Zhongshan",
          "Neihu",
          "Shilin"
        ]
      },
      {
        "name": "Nova Taipé (New Taipei)",
        "code": "NTP",
        "cities": [
          "Banqiao",
          "Zhonghe",
          "Sanchong",
          "Xinzhuang",
          "Xindian",
          "Tamsui"
        ]
      },
      {
        "name": "Taichung",
        "code": "TXG",
        "cities": [
          "Taichung",
          "Xitun",
          "Nantun",
          "Beitun",
          "Fengyuan"
        ]
      },
      {
        "name": "Kaohsiung",
        "code": "KHH",
        "cities": [
          "Kaohsiung",
          "Sanmin",
          "Lingya",
          "Fengshan",
          "Zuoying"
        ]
      },
      {
        "name": "Taoyuan",
        "code": "TYN",
        "cities": [
          "Taoyuan",
          "Zhongli",
          "Bade",
          "Pingzhen"
        ]
      },
      {
        "name": "Tainan",
        "code": "TNN",
        "cities": [
          "Tainan",
          "Yongkang",
          "Annan",
          "East District"
        ]
      },
      {
        "name": "Hsinchu",
        "code": "HSZ",
        "cities": [
          "Hsinchu City",
          "Hsinchu County (Zhubei)"
        ]
      }
    ]
  },
  {
    "name": "Tajiquistão",
    "code": "TJ",
    "states": [
      {
        "name": "Dushanbe",
        "code": "DU",
        "cities": [
          "Dushanbe",
          "Khujand",
          "Bokhtar",
          "Kulob",
          "Istaravshan"
        ]
      }
    ]
  },
  {
    "name": "Tanzânia",
    "code": "TZ",
    "states": [
      {
        "name": "Dar es Salaam",
        "code": "02",
        "cities": [
          "Dar es Salaam",
          "Kinondoni",
          "Ilala",
          "Temeke",
          "Ubungo",
          "Kigamboni"
        ]
      },
      {
        "name": "Dodoma (Capital)",
        "code": "01",
        "cities": [
          "Dodoma"
        ]
      },
      {
        "name": "Mwanza",
        "code": "19",
        "cities": [
          "Mwanza",
          "Nyamagana",
          "Ilemela"
        ]
      },
      {
        "name": "Arusha",
        "code": "01",
        "cities": [
          "Arusha",
          "Moshi"
        ]
      },
      {
        "name": "Zanzibar",
        "code": "15",
        "cities": [
          "Stone Town",
          "Zanzibar City"
        ]
      }
    ]
  },
  {
    "name": "Timor-Leste",
    "code": "TL",
    "states": [
      {
        "name": "Díli",
        "code": "DI",
        "cities": [
          "Díli",
          "Baucau",
          "Maliana",
          "Suai",
          "Liquiçá"
        ]
      }
    ]
  },
  {
    "name": "Togo",
    "code": "TG",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Lomé",
          "Sokodé",
          "Kara",
          "Kpalimé",
          "Atakpamé"
        ]
      }
    ]
  },
  {
    "name": "Tonga",
    "code": "TO",
    "states": [
      {
        "name": "Tonga",
        "code": "TO",
        "cities": [
          "Nukuʻalofa",
          "Neiafu",
          "Haveluloto",
          "Vaini"
        ]
      }
    ]
  },
  {
    "name": "Trinidad e Tobago",
    "code": "TT",
    "states": [
      {
        "name": "Trinidad e Tobago",
        "code": "TT",
        "cities": [
          "Port of Spain",
          "Chaguanas",
          "San Fernando",
          "Arima",
          "Point Fortin",
          "Scarborough"
        ]
      }
    ]
  },
  {
    "name": "Tunísia",
    "code": "TN",
    "states": [
      {
        "name": "Túnis",
        "code": "11",
        "cities": [
          "Túnis",
          "La Marsa",
          "Carthage",
          "Le Kram",
          "Sidi Bou Said"
        ]
      },
      {
        "name": "Sousse",
        "code": "51",
        "cities": [
          "Sousse",
          "Hammam Sousse",
          "Msaken",
          "Kalaa Kebira"
        ]
      },
      {
        "name": "Sfax",
        "code": "61",
        "cities": [
          "Sfax",
          "Sakiet Ezzit",
          "Sakiet Eddaier"
        ]
      },
      {
        "name": "Ariana",
        "code": "12",
        "cities": [
          "Ariana",
          "La Soukra",
          "Mnihla",
          "Raoued"
        ]
      },
      {
        "name": "Ben Arous",
        "code": "13",
        "cities": [
          "Ben Arous",
          "Radès",
          "Hammam Lif",
          "Megrine"
        ]
      },
      {
        "name": "Monastir",
        "code": "52",
        "cities": [
          "Monastir",
          "Moknine",
          "Ksar Hellal",
          "Jemmal"
        ]
      },
      {
        "name": "Nabeul",
        "code": "21",
        "cities": [
          "Nabeul",
          "Hammamet",
          "Kelibia",
          "Grombalia"
        ]
      },
      {
        "name": "Bizerte",
        "code": "23",
        "cities": [
          "Bizerte",
          "Menzel Bourguiba",
          "Mateur"
        ]
      }
    ]
  },
  {
    "name": "Turcomenistão",
    "code": "TM",
    "states": [
      {
        "name": "Ashgabat",
        "code": "S",
        "cities": [
          "Ashgabat",
          "Türkmenabat",
          "Daşoguz",
          "Mary",
          "Balkanabat",
          "Türkmenbaşy"
        ]
      }
    ]
  },
  {
    "name": "Turquia",
    "code": "TR",
    "states": [
      {
        "name": "Istambul",
        "code": "34",
        "cities": [
          "Istambul",
          "Esenyurt",
          "Küçükçekmece",
          "Bağcılar",
          "Pendik",
          "Ümraniye",
          "Kadıköy",
          "Beşiktaş",
          "Bakırköy",
          "Şişli",
          "Beyoğlu",
          "Maltepe",
          "Ataşehir"
        ]
      },
      {
        "name": "Ancara",
        "code": "06",
        "cities": [
          "Ancara",
          "Çankaya",
          "Keçiören",
          "Yenimahalle",
          "Mamak",
          "Etimesgut",
          "Sincan",
          "Altındağ",
          "Gölbaşı"
        ]
      },
      {
        "name": "Esmirna (İzmir)",
        "code": "35",
        "cities": [
          "Esmirna (İzmir)",
          "Karşıyaka",
          "Bornova",
          "Konak",
          "Buca",
          "Bayraklı",
          "Çiğli",
          "Torbalı",
          "Menemen",
          "Gaziemir",
          "Çeşme"
        ]
      },
      {
        "name": "Bursa",
        "code": "16",
        "cities": [
          "Bursa",
          "Osmangazi",
          "Yıldırım",
          "Nilüfer",
          "İnegöl",
          "Gemlik",
          "Mustafakemalpaşa"
        ]
      },
      {
        "name": "Antália",
        "code": "07",
        "cities": [
          "Antália",
          "Kepez",
          "Muratpaşa",
          "Alanya",
          "Manavgat",
          "Konyaaltı",
          "Serik",
          "Kemer",
          "Kaş"
        ]
      },
      {
        "name": "Adana",
        "code": "01",
        "cities": [
          "Adana",
          "Seyhan",
          "Yüreğir",
          "Çukurova",
          "Sariçam",
          "Ceyhan",
          "Kozan"
        ]
      },
      {
        "name": "Cônia (Konya)",
        "code": "42",
        "cities": [
          "Cônia",
          "Selçuklu",
          "Meram",
          "Karatay",
          "Ereğli",
          "Akşehir"
        ]
      },
      {
        "name": "Gaziantep",
        "code": "27",
        "cities": [
          "Gaziantep",
          "Şahinbey",
          "Şehitkamil",
          "Nizip"
        ]
      },
      {
        "name": "Kocaeli",
        "code": "41",
        "cities": [
          "İzmit",
          "Gebze",
          "Darıca",
          "Körfez",
          "Gölcük",
          "Derince",
          "Çayırova"
        ]
      },
      {
        "name": "Mersin",
        "code": "33",
        "cities": [
          "Mersin",
          "Akdeniz",
          "Mezitli",
          "Toroslar",
          "Yenişehir",
          "Tarsus",
          "Erdemli"
        ]
      },
      {
        "name": "Diyarbakır",
        "code": "21",
        "cities": [
          "Diyarbakır",
          "Bağlar",
          "Kayapınar",
          "Yenişehir"
        ]
      },
      {
        "name": "Kayseri",
        "code": "38",
        "cities": [
          "Kayseri",
          "Melikgazi",
          "Kocasinan",
          "Talas"
        ]
      },
      {
        "name": "Eskişehir",
        "code": "26",
        "cities": [
          "Eskişehir",
          "Odunpazarı",
          "Tepebaşı"
        ]
      },
      {
        "name": "Samsun",
        "code": "55",
        "cities": [
          "Samsun",
          "İlkadım",
          "Atakum",
          "Canik",
          "Bafra",
          "Çarşamba"
        ]
      },
      {
        "name": "Denizli",
        "code": "20",
        "cities": [
          "Denizli",
          "Pamukkale",
          "Merkezefendi"
        ]
      },
      {
        "name": "Muğla",
        "code": "48",
        "cities": [
          "Bodrum",
          "Fethiye",
          "Marmaris",
          "Menteşe",
          "Milas"
        ]
      },
      {
        "name": "Trebizonda (Trabzon)",
        "code": "61",
        "cities": [
          "Trabzon",
          "Ortahisar",
          "Akçaabat"
        ]
      }
    ]
  },
  {
    "name": "Tuvalu",
    "code": "TV",
    "states": [
      {
        "name": "Principal",
        "cities": [
          "Funafuti",
          "Vaiaku",
          "Asau"
        ]
      }
    ]
  },
  {
    "name": "Ucrânia",
    "code": "UA",
    "states": [
      {
        "name": "Kiev",
        "code": "30",
        "cities": [
          "Kiev",
          "Bila Tserkva",
          "Brovary",
          "Boryspil",
          "Fastiv",
          "Irpin"
        ]
      },
      {
        "name": "Carcóvia (Kharkiv)",
        "code": "63",
        "cities": [
          "Carcóvia",
          "Lozova",
          "Izium",
          "Chuhuiv"
        ]
      },
      {
        "name": "Lviv",
        "code": "46",
        "cities": [
          "Lviv",
          "Drohobych",
          "Chervonohrad",
          "Stryi",
          "Sambir"
        ]
      },
      {
        "name": "Odessa",
        "code": "51",
        "cities": [
          "Odessa",
          "Izmail",
          "Chornomorsk",
          "Bilhorod-Dnistrovskyi"
        ]
      },
      {
        "name": "Dnipro",
        "code": "12",
        "cities": [
          "Dnipro",
          "Kryvyi Rih",
          "Kamianske",
          "Nikopol",
          "Pavlohrad"
        ]
      },
      {
        "name": "Zaporizhzhia",
        "code": "23",
        "cities": [
          "Zaporizhzhia",
          "Melitopol",
          "Berdiansk"
        ]
      },
      {
        "name": "Vinnytsia",
        "code": "05",
        "cities": [
          "Vinnytsia",
          "Zhmerynka",
          "Mohyliv-Podilskyi"
        ]
      },
      {
        "name": "Ivano-Frankivsk",
        "code": "26",
        "cities": [
          "Ivano-Frankivsk",
          "Kalush",
          "Kolomyia"
        ]
      },
      {
        "name": "Poltava",
        "code": "53",
        "cities": [
          "Poltava",
          "Kremenchuk",
          "Lubny",
          "Myrhorod"
        ]
      },
      {
        "name": "Cherkasy",
        "code": "71",
        "cities": [
          "Cherkasy",
          "Uman",
          "Smila"
        ]
      },
      {
        "name": "Zhytomyr",
        "code": "18",
        "cities": [
          "Zhytomyr",
          "Berdychiv",
          "Korosten",
          "Novohrad-Volynskyi"
        ]
      },
      {
        "name": "Chernivtsi",
        "code": "77",
        "cities": [
          "Chernivtsi",
          "Storozhynets",
          "Novodnistrovsk"
        ]
      },
      {
        "name": "Rivne",
        "code": "56",
        "cities": [
          "Rivne",
          "Varash",
          "Dubno",
          "Kostopil"
        ]
      },
      {
        "name": "Ternopil",
        "code": "61",
        "cities": [
          "Ternopil",
          "Chortkiv",
          "Kremenets"
        ]
      },
      {
        "name": "Transcarpátia",
        "code": "21",
        "cities": [
          "Uzhhorod",
          "Mukachevo",
          "Khust",
          "Berehove"
        ]
      }
    ]
  },
  {
    "name": "Uganda",
    "code": "UG",
    "states": [
      {
        "name": "Região Central",
        "code": "C",
        "cities": [
          "Kampala",
          "Entebbe",
          "Nansana",
          "Kira",
          "Mukono",
          "Masaka"
        ]
      },
      {
        "name": "Região Ocidental",
        "code": "W",
        "cities": [
          "Mbarara",
          "Gulu",
          "Jinja",
          "Mbale",
          "Fort Portal"
        ]
      }
    ]
  },
  {
    "name": "Uruguai",
    "code": "UY",
    "states": [
      {
        "name": "Montevideo",
        "code": "MO",
        "cities": [
          "Montevideo",
          "Pocitos",
          "Carrasco",
          "Centro",
          "Cordón",
          "Punta Carretas"
        ]
      },
      {
        "name": "Canelones",
        "code": "CA",
        "cities": [
          "Ciudad de la Costa",
          "Las Piedras",
          "Pando",
          "Canelones",
          "Barros Blancos",
          "Santa Lucía",
          "Progreso",
          "Paso Carrasco"
        ]
      },
      {
        "name": "Maldonado",
        "code": "MA",
        "cities": [
          "Maldonado",
          "Punta del Este",
          "San Carlos",
          "Piriápolis",
          "Pan de Azúcar"
        ]
      },
      {
        "name": "Salto",
        "code": "SA",
        "cities": [
          "Salto",
          "Constitución",
          "Belén"
        ]
      },
      {
        "name": "Paysandú",
        "code": "PA",
        "cities": [
          "Paysandú",
          "Guichón",
          "Quebracho"
        ]
      },
      {
        "name": "Colonia",
        "code": "CO",
        "cities": [
          "Colonia del Sacramento",
          "Carmelo",
          "Nueva Helvecia",
          "Juan Lacaze",
          "Rosario"
        ]
      },
      {
        "name": "Rivera",
        "code": "RV",
        "cities": [
          "Rivera",
          "Tranqueras",
          "Vichadero"
        ]
      },
      {
        "name": "Tacuarembó",
        "code": "TA",
        "cities": [
          "Tacuarembó",
          "Paso de los Toros",
          "San Gregorio de Polanco"
        ]
      },
      {
        "name": "San José",
        "code": "SJ",
        "cities": [
          "San José de Mayo",
          "Ciudad del Plata",
          "Libertad"
        ]
      },
      {
        "name": "Soriano",
        "code": "SO",
        "cities": [
          "Mercedes",
          "Dolores",
          "Cardona"
        ]
      },
      {
        "name": "Cerro Largo",
        "code": "CL",
        "cities": [
          "Melo",
          "Río Branco",
          "Fraile Muerto"
        ]
      },
      {
        "name": "Rocha",
        "code": "RO",
        "cities": [
          "Rocha",
          "Chuy",
          "Castillos",
          "La Paloma"
        ]
      },
      {
        "name": "Florida",
        "code": "FD",
        "cities": [
          "Florida",
          "Sarandí Grande",
          "Casupá"
        ]
      },
      {
        "name": "Lavalleja",
        "code": "LA",
        "cities": [
          "Minas",
          "José Pedro Varela",
          "Solís de Mataojo"
        ]
      },
      {
        "name": "Durazno",
        "code": "DU",
        "cities": [
          "Durazno",
          "Sarandí del Yí",
          "Carmen"
        ]
      },
      {
        "name": "Treinta y Tres",
        "code": "TT",
        "cities": [
          "Treinta y Tres",
          "Santa Clara de Olimar",
          "Vergara"
        ]
      },
      {
        "name": "Río Negro",
        "code": "RN",
        "cities": [
          "Fray Bentos",
          "Young",
          "Nuevo Berlín"
        ]
      },
      {
        "name": "Artigas",
        "code": "AR",
        "cities": [
          "Artigas",
          "Bella Unión",
          "Tomás Gomensoro"
        ]
      },
      {
        "name": "Flores",
        "code": "FS",
        "cities": [
          "Trinidad",
          "Ismael Cortinas"
        ]
      }
    ]
  },
  {
    "name": "Uzbequistão",
    "code": "UZ",
    "states": [
      {
        "name": "Tashkent",
        "code": "TO",
        "cities": [
          "Tashkent",
          "Chirchiq",
          "Olmaliq",
          "Angren"
        ]
      },
      {
        "name": "Samarcanda",
        "code": "SA",
        "cities": [
          "Samarcanda",
          "Kattaqo'rg'on"
        ]
      },
      {
        "name": "Fergana",
        "code": "FA",
        "cities": [
          "Fergana",
          "Kokand",
          "Margilan"
        ]
      },
      {
        "name": "Bucara",
        "code": "BU",
        "cities": [
          "Bucara",
          "G'ijduvon",
          "Kogon"
        ]
      },
      {
        "name": "Andijon",
        "code": "AN",
        "cities": [
          "Andijon",
          "Asaka",
          "Shahrixon"
        ]
      },
      {
        "name": "Namangan",
        "code": "NG",
        "cities": [
          "Namangan",
          "Chust",
          "Kosonsoy"
        ]
      }
    ]
  },
  {
    "name": "Vanuatu",
    "code": "VU",
    "states": [
      {
        "name": "Vanuatu",
        "code": "VU",
        "cities": [
          "Port Vila",
          "Luganville",
          "Norsup"
        ]
      }
    ]
  },
  {
    "name": "Vaticano",
    "code": "VA",
    "states": [
      {
        "name": "Vaticano",
        "code": "VA",
        "cities": [
          "Cidade do Vaticano"
        ]
      }
    ]
  },
  {
    "name": "Venezuela",
    "code": "VE",
    "states": [
      {
        "name": "Distrito Capital",
        "code": "DC",
        "cities": [
          "Caracas",
          "El Hatillo",
          "Chacao",
          "Baruta",
          "Sucre"
        ]
      },
      {
        "name": "Zulia",
        "code": "ZU",
        "cities": [
          "Maracaibo",
          "Cabimas",
          "Ciudad Ojeda",
          "San Francisco"
        ]
      },
      {
        "name": "Miranda",
        "code": "MI",
        "cities": [
          "Los Teques",
          "Guarenas",
          "Guatire",
          "Petare",
          "Charallave"
        ]
      },
      {
        "name": "Carabobo",
        "code": "CA",
        "cities": [
          "Valencia",
          "Puerto Cabello",
          "Guacara",
          "Naguanagua",
          "San Diego"
        ]
      },
      {
        "name": "Lara",
        "code": "LA",
        "cities": [
          "Barquisimeto",
          "Cabudare",
          "Carora",
          "El Tocuyo"
        ]
      },
      {
        "name": "Aragua",
        "code": "AR",
        "cities": [
          "Maracay",
          "Turmero",
          "La Victoria",
          "Cagua"
        ]
      },
      {
        "name": "Bolívar",
        "code": "BO",
        "cities": [
          "Ciudad Guayana",
          "Ciudad Bolívar",
          "Upata",
          "Puerto Ordaz"
        ]
      },
      {
        "name": "Anzoátegui",
        "code": "AN",
        "cities": [
          "Barcelona",
          "Puerto La Cruz",
          "El Tigre",
          "Lechería",
          "Anaco"
        ]
      },
      {
        "name": "Táchira",
        "code": "TA",
        "cities": [
          "San Cristóbal",
          "Táriba",
          "Rubio",
          "San Antonio del Táchira"
        ]
      },
      {
        "name": "Mérida",
        "code": "ME",
        "cities": [
          "Mérida",
          "El Vigía",
          "Ejido",
          "Tovar"
        ]
      },
      {
        "name": "Falcón",
        "code": "FA",
        "cities": [
          "Coro",
          "Punto Fijo",
          "Puerto Cumarebo"
        ]
      },
      {
        "name": "Nueva Esparta",
        "code": "NE",
        "cities": [
          "Porlamar",
          "Pampatar",
          "La Asunción",
          "Juan Griego"
        ]
      }
    ]
  },
  {
    "name": "Vietnã",
    "code": "VN",
    "states": [
      {
        "name": "Ho Chi Minh (Saigon)",
        "code": "SG",
        "cities": [
          "Ho Chi Minh",
          "Thu Duc",
          "Distrito 1",
          "Distrito 7",
          "Binh Thanh",
          "Tan Binh"
        ]
      },
      {
        "name": "Hanói",
        "code": "HN",
        "cities": [
          "Hanói",
          "Ba Dinh",
          "Hoan Kiem",
          "Cau Giay",
          "Dong Da",
          "Hai Ba Trung",
          "Tay Ho"
        ]
      },
      {
        "name": "Da Nang",
        "code": "DN",
        "cities": [
          "Da Nang",
          "Hai Chau",
          "Son Tra",
          "Ngu Hanh Son"
        ]
      },
      {
        "name": "Hai Phong",
        "code": "HP",
        "cities": [
          "Hai Phong",
          "Ngo Quyen",
          "Hong Bang"
        ]
      },
      {
        "name": "Can Tho",
        "code": "CT",
        "cities": [
          "Can Tho",
          "Ninh Kieu",
          "Binh Thuy"
        ]
      },
      {
        "name": "Binh Duong",
        "code": "BD",
        "cities": [
          "Thu Dau Mot",
          "Di An",
          "Thuan An"
        ]
      },
      {
        "name": "Dong Nai",
        "code": "DN",
        "cities": [
          "Bien Hoa",
          "Long Khanh"
        ]
      },
      {
        "name": "Khanh Hoa",
        "code": "KH",
        "cities": [
          "Nha Trang",
          "Cam Ranh"
        ]
      },
      {
        "name": "Ba Ria - Vung Tau",
        "code": "VT",
        "cities": [
          "Vung Tau",
          "Ba Ria"
        ]
      },
      {
        "name": "Thua Thien Hue",
        "code": "TT",
        "cities": [
          "Hue"
        ]
      }
    ]
  },
  {
    "name": "Zâmbia",
    "code": "ZM",
    "states": [
      {
        "name": "Lusaka",
        "code": "09",
        "cities": [
          "Lusaka",
          "Chilanga",
          "Chongwe",
          "Kafue"
        ]
      },
      {
        "name": "Copperbelt",
        "code": "08",
        "cities": [
          "Kitwe",
          "Ndola",
          "Chingola",
          "Mufulira",
          "Luanshya"
        ]
      },
      {
        "name": "Sul",
        "code": "07",
        "cities": [
          "Livingstone",
          "Choma",
          "Mazabuka"
        ]
      }
    ]
  },
  {
    "name": "Zimbábue",
    "code": "ZW",
    "states": [
      {
        "name": "Harare",
        "code": "HA",
        "cities": [
          "Harare",
          "Chitungwiza",
          "Epworth",
          "Ruwa"
        ]
      },
      {
        "name": "Bulawayo",
        "code": "BU",
        "cities": [
          "Bulawayo"
        ]
      },
      {
        "name": "Manicaland",
        "code": "MA",
        "cities": [
          "Mutare",
          "Rusape"
        ]
      },
      {
        "name": "Midlands",
        "code": "MI",
        "cities": [
          "Gweru",
          "Kwekwe"
        ]
      }
    ]
  }
];

export function getCountryOptions(): LocationOption[] {
  return LOCATION_DATA.map((c) => ({
    value: c.name,
    label: c.name,
  }));
}

export function findCountry(countryNameOrCode: string | null | undefined): CountryLocation | undefined {
  if (!countryNameOrCode) return undefined;
  const raw = countryNameOrCode.trim().toLowerCase();
  return LOCATION_DATA.find(
    (c) => c.name.toLowerCase() === raw || c.code.toLowerCase() === raw,
  );
}

export function getStatesForCountry(countryNameOrCode: string | null | undefined): LocationOption[] {
  const country = findCountry(countryNameOrCode);
  if (!country) return [];
  return country.states.map((s) => {
    // Só exibe a sigla se for alfabética reconhecida (ex: SP, RJ, TX, CA, BA) e não códigos numéricos
    const hasAlphaCode = s.code && !/^\d+$/.test(s.code) && s.code !== s.name && s.code.length <= 4;
    return {
      value: s.name,
      label: hasAlphaCode ? `${s.name} (${s.code})` : s.name,
    };
  });
}

export function findState(
  countryNameOrCode: string | null | undefined,
  stateNameOrCode: string | null | undefined,
): StateLocation | undefined {
  const country = findCountry(countryNameOrCode);
  if (!country || !stateNameOrCode) return undefined;
  const rawState = stateNameOrCode.trim().toLowerCase();
  return country.states.find(
    (s) => s.name.toLowerCase() === rawState || (s.code && s.code.toLowerCase() === rawState),
  );
}

export function getCitiesForState(
  countryNameOrCode: string | null | undefined,
  stateNameOrCode: string | null | undefined,
): string[] {
  const state = findState(countryNameOrCode, stateNameOrCode);
  if (!state) return [];
  return [...state.cities].sort((a, b) => a.localeCompare(b, 'pt-BR'));
}

export function normalizeCountry(countryInput: string | null | undefined): string {
  if (!countryInput) return '';
  const country = findCountry(countryInput);
  return country ? country.name : countryInput.trim();
}

export function normalizeState(
  countryInput: string | null | undefined,
  stateInput: string | null | undefined,
): string {
  if (!stateInput) return '';
  const state = findState(countryInput, stateInput);
  return state ? state.name : stateInput.trim();
}

export function getCountryCode(countryNameOrCode: string | null | undefined): string | null {
  if (!countryNameOrCode) return null;
  const country = findCountry(countryNameOrCode);
  if (country) return country.code.toLowerCase();
  const trimmed = countryNameOrCode.trim();
  if (trimmed.length === 2) return trimmed.toLowerCase();
  return null;
}

export function getCountryFlagUrl(countryNameOrCode: string | null | undefined): string | null {
  const code = getCountryCode(countryNameOrCode);
  if (!code || code.length !== 2) return null;
  return `https://flagcdn.com/w40/${code}.png`;
}

export function formatLocation(
  country: string | null | undefined,
  state: string | null | undefined,
  city: string | null | undefined,
): string {
  const parts: string[] = [];
  if (city?.trim()) parts.push(city.trim());
  if (state?.trim()) {
    const normState = normalizeState(country, state);
    parts.push(normState || state.trim());
  }
  if (country?.trim()) {
    const normCountry = normalizeCountry(country);
    parts.push(normCountry || country.trim());
  }
  return parts.join(', ');
}
