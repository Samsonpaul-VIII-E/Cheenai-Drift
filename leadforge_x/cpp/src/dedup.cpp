#include "leadforge_core.hpp"
#include <unordered_set>

namespace leadforge {
std::size_t unique_count(const std::vector<std::string>& values) {
    std::unordered_set<std::string> set(values.begin(), values.end());
    return set.size();
}
}
