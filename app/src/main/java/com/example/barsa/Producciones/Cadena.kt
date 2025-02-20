package com.example.barsa.Producciones

interface Cadena {
    val nextProcess: Cadena?
    val processName: String

    fun handleProcess(currentProcess: Int, onProcessCompleted: () -> Unit) {
        if (currentProcess == 1 && this is MaderaProcess ||
            currentProcess == 2 && this is PinturaProcess ||
            currentProcess == 3 && this is TapiceriaProcess ||
            currentProcess == 4 && this is EmpaqueProcess
        ) {
            onProcessCompleted()
        } else {
            nextProcess?.handleProcess(currentProcess, onProcessCompleted)
        }
    }
}

class MaderaProcess(override val nextProcess: Cadena?) : Cadena {
    override val processName = "Madera"
}

class PinturaProcess(override val nextProcess: Cadena?) : Cadena {
    override val processName = "Pintura"
}

class TapiceriaProcess(override val nextProcess: Cadena?) : Cadena {
    override val processName = "Tapicería"
}

class EmpaqueProcess(override val nextProcess: Cadena?) : Cadena {
    override val processName = "Empaque"
}