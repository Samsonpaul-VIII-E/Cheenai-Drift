#include "leadforge_core.hpp"
#include <sstream>
#include <iomanip>
#include <functional>

namespace leadforge {
std::string fingerprint(const std::string& input) {
    const auto h = std::hash<std::string>{}(input);
    std::ostringstream out;
    out << std::hex << std::setw(16) << std::setfill('0') << h;
    return out.str();
}
}
