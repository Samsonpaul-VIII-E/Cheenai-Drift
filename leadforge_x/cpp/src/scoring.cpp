#include "leadforge_core.hpp"
#include <algorithm>
#include <cctype>
#include <string>

namespace {
std::string lower(std::string s) {
    std::transform(s.begin(), s.end(), s.begin(),
                   [](unsigned char c){ return static_cast<char>(std::tolower(c)); });
    return s;
}
}

namespace leadforge {
double keyword_score(const std::string& text, const std::vector<std::string>& keywords) {
    if (keywords.empty()) return 0.0;
    const std::string t = lower(text);
    std::size_t hits = 0;
    for (const auto& k : keywords) {
        if (!k.empty() && t.find(lower(k)) != std::string::npos) ++hits;
    }
    return static_cast<double>(hits) / static_cast<double>(keywords.size());
}
}
