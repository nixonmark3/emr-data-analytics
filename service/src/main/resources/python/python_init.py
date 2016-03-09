import sys, getopt, traceback
import json
import numpy as np
import pandas as pd

from py4j.java_gateway import java_import, JavaGateway, GatewayClient
from py4j.protocol import Py4JJavaError

# reference the py4J client
client = GatewayClient(port=int(sys.argv[1]))
gateway = JavaGateway(client, auto_convert = True)

# reference interpreter
interpreter = gateway.entry_point