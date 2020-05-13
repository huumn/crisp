Crisp is a cryptocurrency written in Lisp. As of this writing, it's a toy blockchain written in Chez Scheme. 

## Goals

1. Make Crisp a robust blockchain implementation written in Lisp that also uses Lisp as the transaction language.
2. Experiment with the transaction language such that it provides features unavailable in existing blockchains

## Control

Crisp listens on a TCP port for control commands. Commands can be sent pretty naturally with telnet as commands are framed by the delimiter `\r\n`:

```
> blocks
> [{"index":0,"prev-hash":"","timestamp":1465154705,"data":"Genisis Block."}]
```

A command name and its arguments are delimited with spaces:

```
> peer-add 127.0.0.1 9998
> OK
```

#### Control Commands
| command       | returns       |
| ------------- | ------------- |
| blocks        | json array of blocks |
| block-mine [msg] | newly mined block as json     |
| block-last | the last block mined |
| peers | json array of peers as ip:port |
| peer-add ip port | OK |
| echo msg | msg | 


## Peer Gossip

Crisp peers gossip over TCP. Messages are encoded as ascii-length-prefixed JSON, where the message length and the messages themselves are delimited with `\r\n`. The JSON messages contain both a `type` and `data` field. When a peer asks another peer for its last block, the message looks like the following:

```
"31\r\n{\"type\":\"last-block\",\"data\":\"\"}\r\n"
```

#### Peer Message types and data
| type | data |
| --- | --- |
|last-block| no data expected|
|last-block-resp | json encoded block |
|blocks | no data expected |
|blocks-resp | json array of blocks|

## Consensus

The valid chain with the most cumulative work always wins. Because we don't yet have transactions, valid here only means the chain itself is a valid blockchain. That is, the blocks are "linked" with the correct hash "pointers," and the timestamps between blocks is reasonable.

## Running

For the time being Crisp uses [ravensc.com](http://ravensc.com) for package control and uses [suv](http://github.com/huumn/suv) for io which you'll have to clone and build manually. The full process from scratch, assuming you already have [chez](https://github.com/cisco/ChezScheme) and aren't on Windows:

```
$ curl -L http://ravensc.com/install | scheme
$ git clone git@github.com:huumn/crisp.git
$ cd crisp
$ raven install
$ cd lib
$ git clone git@github.com:huumn/suv.git
$ cd suv 
$ cc -o3 -fPIC -shared c/suv.c -luv -o libsuv.so
$ cd ../..
$ scheme --script main.sc
```

You should then be able to connect to your Crisp node via telnet:
```
$ telnet 127.0.0.1 7777
echo hello
hello
blocks
[{"index":0,"prev-hash":"","timestamp":1465154705,"data":"Genisis Block."}]
```
