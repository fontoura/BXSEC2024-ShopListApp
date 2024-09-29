# Aplicativo de Lista de Compras para Android

> 🇺🇸 To see this file in English, click [here](./README.md)

Esta é uma implementação open-source de um aplicativo de lista de compras para Android.

Este aplicativo foi implementado para ser utilizado na apresentação da [ThaySolis](https://github.com/ThaySolis) sobre engenharia reversa na Conferência Baixada Santista Security (BXSEC) de 2024. Para configurar e usar o aplicativo, siga estes passos:

1. Configurar uma máquina servidor com um IP público.
2. Instalar e iniciar o servidor de API na máquina servidor.
3. Criar um registro DNS apontando para o IP público da máquina servidor.
4. Atualizar o URL do servidor de API no código-fonte Android.
5. Compilar o APK.
6. Instalar o APK no seu dispositivo.

Para instruções mais detalhadas, consulte o guia abaixo.

# Guia

## Configurar uma máquina servidor com um IP público.

Para executar o servidor de API, você precisará de uma máquina com um endereço IP público. Muito provavelmente, você acabará usando um provedor de nuvem como AWS para isso. Além de configurar a máquina, é necessário configurar regras de firewall para permitir o acesso externo à porta `8443` dela a partir de qualquer lugar (ou seja, `0.0.0.0/0`). Siga os passos abaixo se estiver utilizando AWS:

- **Crie uma instância EC2**: Selecione uma imagem Linux (por exemplo, Ubuntu) e certifique-se de que ela tenha um endereço IP público.
- **Crie e configure um Security Group**: Configure um Security Group personalizado e associe-o à sua instância EC2.
- A**tualize as regras do Security Group**: No Security Group, modifique as regras de entrada (ingress) para permitir conexões TCP de `0.0.0.0/0` (ou seja, de qualquer lugar) para a porta `8443`.

# Instalar e iniciar o servidor de API na máquina servidor

Após criar a máquina servidor, certifique-se de que o Go esteja instalado. Você pode seguir o [guia oficial de instalação](https://go.dev/doc/install) ou, se estiver usando o Ubuntu, pode simplesmente executar o seguinte comando:

```bash
apt install go
```

Uma vez que o Go estiver instalado, transfira a pasta `server` para a sua máquina servidor. É recomendado colocá-la no seu diretório pessoal, mas qualquer local no sistema de arquivos funcionará. O processo para transferir os arquivos vai variar dependendo do seu provedor de nuvem e da configuração da máquina.

Navegue até a pasta `server` na máquina servidor e compile o servidor de API através do comando:

```bash
go build .
```

Se a compilação for bem-sucedida, um novo arquivo chamado `shoplistvulnerableserver` será criado na mesma pasta. Este é o executável do servidor de API.

Para iniciar o servidor, execute o seguinte comando. É recomendado usar uma ferramenta como o `nohup` para manter o servidor em execução, mesmo se a sessão do console for encerrada:

```bash
nohup ./shoplistvulnerableserver &
```

## Criar um registro DNS apontando para o IP público da máquina servidor

Em seguida, você precisará criar um registro DNS que aponte para o IP público do seu servidor. Existem vários serviços disponíveis para esta finalidade, como o [DuckDNS](https://www.duckdns.org/).

A entrada DNS que você criar servirá como o URL-base para o seu servidor de API. Por exemplo, se sua entrada DNS for `whatever.duckdns.org`, o URL-base da sua API será `https://whatever.duckdns.org:8443`.

Certifique-se de tomar nota de seu URL, pois você precisará dele para os próximos passos.


## Atualizar o URL do servidor de API no código-fonte Android

Localize o arquivo `ShopListApplication.java` no diretório do projeto ([este arquivo](app\src\main\java\com\github\fontoura\sample\shoplist\ShopListApplication.java)). No código Java, procure pela constante `HTTP_ENDPOINT`, que deve estar assim:

```java
private static final String HTTP_ENDPOINT = "https://{INSERT_URL_HERE}:8443";
```

Substitua o valor desta constante pelo URL base do seu servidor de API. Por exemplo:

```java
private static final String HTTP_ENDPOINT = "https://whatever.duckdns.org:8443";
```

Certifique-se de usar o URL certo, para refletir a entrada DNS criada no passo anterior.

## Compilar o APK

Certifique-se de que o Android Studio esteja instalado. Se não estiver, faça o download no [site oficial](https://developer.android.com/studio) e proceda com a instalação.

1. Abra o projeto no Android Studio.
2. Quando o projeto estiver completamente carregado, selecione a opção **Build > Generate Signed App Bundle / APK...**.
3. Na janela pop-up, selecione a opção **APK**.
4. Se você ainda não tiver um keystore, proceda com a criação de um, juntamente com uma chave.
5. Selecione a variante de build **release**.

O APK será gerado na pasta `app/release`.

## Instalar o APK no seu dispositivo

Para instalar o APK recém-gerado no seu dispositivo, há duas opções:

1. Usando o ADB:
    - Conecte seu dispositivo ao computador via USB.
    - Execute o seguinte comando no seu terminal ou prompt de comando:
    ```
    adb install <caminho-do-arquivo>/release.apk
    ```
2. Instalação manual:
    - Transfira o arquivo APK para o seu dispositivo (por exemplo, via USB ou armazenamento em nuvem).
    - Abra o APK no seu dispositivo e siga as instruções para instalá-lo.

# Licença
Este projeto está licenciado sob a Licença MIT. Consulte o arquivo [LICENSE](./LICENSE) para mais detalhes.
