package config;

import model.Sex;

import java.util.*;

/**
 * Provides unique, pre-generated names for hedgehogs based on sex.
 * Names are shuffled at initialization and consumed sequentially.
 * Throws an exception if names run out.
 */
public class HedgehogNameManager {

    private final Queue<String> maleNames;
    private final Queue<String> femaleNames;

    public HedgehogNameManager() {
        maleNames = new LinkedList<>();
        femaleNames = new LinkedList<>();

        initializeMaleNames();
        initializeFemaleNames();
    }

    private void initializeMaleNames() {
        List<String> names = Arrays.asList(
                "Branburr", "Fionnprick", "Thornwyn", "Cairnach", "Eogan Quillsharp",
                "Briarnach", "Aedán of the Den", "Cormac Spiketail", "Donnach MacSpine", "Fergus Briarfoot",
                "Lorcan Hedgebane", "Ronan the Bristled", "Seanan O’Quill", "Daithi Thorncloak", "Colman Spineshade",
                "Padraig Quickspike", "Faelan Burrclaw", "Malachy Hedgeborn", "Conall Fairysworn", "Breccan Sharpburrow",
                "Oisin Thornfang", "Finbar Denwatcher", "Niall Quillkin", "Cathal Briarpaw", "Ruairi Hedgewarden",
                "Turlough of the Hollow", "Ciaran Sharpnose", "Tiernan of the Brambles", "Dara Spikewarden", "Artan Quickthorn",
                "Eoin Fairyleaf", "Donnchadh Burrback", "Diarmaid Quillmarch", "Fearghas of the Underbrush", "Gearoid Spineshield",
                "Lorcan Thorncloak", "Muiris Denborn", "Odhran Hedgefang", "Riordan of the Glen", "Tadhg Quickburrow",
                "Breandán Sharpfoot", "Caolan O’Spine", "Piaras Bristleback", "Ultan the Watchful", "Eochaidh of the Hollow",
                "Iarlaith Thorncloak", "Fintan Hedgebriar", "Colm of the Thistledown", "Séamus Quillwarden", "Torcan Briarborn",
                "Aonghus Thornfang", "Barrin Bristleback", "Caedmon Hedgewarden", "Conrí Burrclaw", "Dara O'Quill",
                "Eirnin Spiketail", "Faolán Sharpburrow", "Gearalt of the Den", "Iollan Thorncloak", "Keelan Briarfoot",
                "Lugh Quickspike", "Murchadh Spineshield", "Naoise Hedgebriar", "Orin Quillkin", "Pádraic Fairysworn",
                "Rioghan Bristledown", "Setanta of the Glen", "Tighearnán Denborn", "Ultán Hedgefang", "Caoimhín Burrback",
                "Branán Thornshade", "Eochaidh Spikewarden", "Feargan Briarpaw", "Guaire of the Hollow", "Iarlaith Sharpnose",
                "Cathán Hedgeborn", "Lorcán Fairycloak", "Odhrán Quickthorn", "Rónán the Bristled", "Séadhna Thornfang",
                "Torin Spiketail", "Uilliam Brambleback", "Énna Hedgewarden", "Béccán Quillmarch", "Connla Briarborn",
                "Eóghan Denwatcher", "Finnian Thornshade", "Cormán Fairypaw", "Darragh Spineshield", "Fachtna of the Thistledown",
                "Muiris Quickburrow", "Tuathal Quillbright", "Tadhg Briarback", "Fionnbharr Hedgefang", "Macdara Sharpfoot",
                "Eimhin Thorncloak", "Cuan Burrclaw", "Breasal the Watchful", "Dáire Bristleborn", "Cianán of the Glen"
        );

        Collections.shuffle(names);
        maleNames.addAll(names);
    }

    private void initializeFemaleNames() {
        List<String> names = Arrays.asList(
                "Ainepike", "Brianna Quilltail", "Thornwyn", "Nuala Spineshade", "Maeve Briarfoot",
                "Siobhan the Sharp", "Eithne Hedgeleaf", "Sorcha Quillcloak", "Roisin Thornborn", "Ailis of the Den",
                "Orla Burrtail", "Clíodhna Hedgefang", "Deirdre of the Hollow", "Branna Sharpburrow", "Sadhbh Fairysworn",
                "Liadan Thornshade", "Aisling Quillbright", "Fionnuala Hedgewarden", "Muireann Briarback", "Grainne of the Thistledown",
                "Caoimhe Spineshield", "Eabha Burrclaw", "Riona Quickspike", "Bláthnaid of the Glen", "Etain Fairycloak",
                "Aoibhe Thornfang", "Dervla Hedgeborn", "Maebh Briarpaw", "Sinead of the Underbrush", "Bríd Quickthorn",
                "Ailbhe Denwatcher", "Niamh Sharpnose", "Lasairíona Quillmarch", "Róisín Fairyleaf", "Dearbháil Hedgebriar",
                "Fíona Thorncloak", "Treasa of the Hollow", "Áine Quillwarden", "Eirinn Burrback", "Saorla Briarborn",
                "Cadhla Spiketail", "Emer of the Brambles", "Muireall Quickburrow", "Iseult Fairypaw", "Macha Hedgefang",
                "Bláithín of the Glen", "Siofra Thorncloak", "Oona Spineshade", "Eilis Denborn", "Aibreann Quillkin",
                "Ailionóra Briarfoot", "Bláth Spineshade", "Caoilfhionn Quickspike", "Daireann Hedgewarden", "Eireann Briarborn",
                "Fiadh Fairysworn", "Gráinne of the Hollow", "Íde Quilltail", "Lasair Hedgefang", "Mairéad Denborn",
                "Nóirín Sharpburrow", "Orfhlaith Brambleback", "Pádraigín of the Glen", "Róisín Spineshield", "Saoirse Quickthorn",
                "Treasa Briarpaw", "Úna Fairycloak", "Aoibhinn Thornfang", "Brídín Hedgeborn", "Ciara Thornshade",
                "Dearbhla Quillmarch", "Éabha Burrback", "Fionnuala Denwatcher", "Gobnait Fairysworn", "Iseult Spiketail",
                "Líadan Hedgewarden", "Doireann Briarback", "Nessa of the Thistledown", "Oighrig Quickburrow", "Peig Bristleborn",
                "Róise Thorncloak", "Sadhbh Fairypaw", "Síle Hedgefang", "Tlachtga Briarfoot", "Ula Quillbright",
                "Áine of the Hollow", "Béibhinn Spineshield", "Clíodhna Thornborn", "Deirbhile Hedgewarden", "Eithne Burrclaw",
                "Fíona Denborn", "Gormlaith Fairysworn", "Íona Sharpnose", "Labhaoise Briarback", "Maebh the Watchful",
                "Nuala Quillwarden", "Orlaith Bristledown", "Ríona Quickspike", "Saorla of the Brambles", "Treasa Thornshade"
        );

        Collections.shuffle(names);
        femaleNames.addAll(names);
    }

    public String getNextName(Sex sex) {
        switch (sex) {
            case MALE -> {
                if (maleNames.isEmpty()) {
                    throw new IllegalStateException("No more male names available!");
                }
                return maleNames.poll();
            }
            case FEMALE -> {
                if (femaleNames.isEmpty()) {
                    throw new IllegalStateException("No more female names available!");
                }
                return femaleNames.poll();
            }
            default -> throw new IllegalArgumentException("Unknown sex: " + sex);
        }
    }

}
